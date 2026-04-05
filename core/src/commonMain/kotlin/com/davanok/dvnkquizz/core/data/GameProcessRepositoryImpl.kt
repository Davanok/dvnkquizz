package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.FullGameSessionDto
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.JudgeAnswerRequest
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.repositories.GameProcessRepository
import com.davanok.dvnkquizz.core.platform.Platform
import com.davanok.dvnkquizz.core.platform.currentPlatform
import com.davanok.dvnkquizz.core.utils.currentUserId
import com.davanok.dvnkquizz.core.utils.div
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.storage.DownloadStatus
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.downloadAuthenticatedAsFlow
import io.ktor.utils.io.asByteWriteChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameProcessRepositoryImpl(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val storage: Storage,
    private val auth: Auth,
    logger: Logger
) : GameProcessRepository {

    private val logger = logger.withTag(TAG)

    override val HEARTBEAT_TIMEOUT_MS: Long = 30_000

    /**
     * Downloads media file with progress reporting and local caching.
     */
    private fun downloadMediaAsFlow(question: QuestionDto): Flow<Question> = flow {
        val mediaUrl = question.mediaUrl ?: return@flow
        val extension = mediaUrl.substringAfterLast('.', "bin")
        val filename = "${question.id}_${mediaUrl.hashCode()}.$extension"

        val localPath = SystemTemporaryDirectory / filename
        val tmpPath = SystemTemporaryDirectory / "$filename.tmp"

        // 1. Check Cache
        if (SystemFileSystem.exists(localPath)) {
            emit(question.toDomain(mediaUrl = localPath.toString(), progress = 1f))
            return@flow
        }

        // 2. Download to Temp File
        runCatching {
            SystemFileSystem.sink(tmpPath).buffered().use { sink ->
                storage.from("questions")
                    .downloadAuthenticatedAsFlow(mediaUrl, channel = sink.asByteWriteChannel())
                    .collect { status ->
                        when (status) {
                            is DownloadStatus.Progress -> {
                                val progress = status.totalBytesReceived.toFloat() / status.contentLength.toFloat()
                                emit(question.toDomain(mediaUrl = tmpPath.toString(), progress = progress))
                            }
                            is DownloadStatus.Success -> {
                                // Handled by atomic move after collection
                            }
                            is DownloadStatus.ByteData -> {
                                logger.w { "Unexpected ByteData received during channel download" }
                            }
                        }
                    }
            }
            SystemFileSystem.atomicMove(tmpPath, localPath)
            emit(question.toDomain(mediaUrl = localPath.toString(), progress = 1f))
        }.onFailure { thr ->
            if (SystemFileSystem.exists(tmpPath)) SystemFileSystem.delete(tmpPath)
            logger.e(thr) { "Failed to download media for question ${question.id}" }
            throw thr
        }
    }.retry(DOWNLOAD_MEDIA_RETRIES)

    private fun getQuestionFlowHelper(sessionId: Uuid, question: QuestionDto): Flow<Question> = flow {
        runCatching {
            when {
                question.mediaKind == MediaKind.NONE || question.mediaUrl == null ->
                    emit(question.toDomain())

                Platform.currentPlatform() == Platform.Web ->
                    emit(question.toDomain(
                        mediaUrl = storage.from("questions")
                            .createSignedUrl(question.mediaUrl, MEDIA_URL_EXPIRE_DURATION),
                        progress = 1f
                    ))

                else -> emitAll(downloadMediaAsFlow(question))
            }
        }
        // Notify server that this client has loaded assets and is ready to display
        markMeAsReady(sessionId)
    }

    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> = channelFlow {
        val topic = "game-state-update:${sessionId.toHexDashString()}"
        val channel = realtime.channel(sessionId.toString())

        channel.subscribe(blockUntilSubscribed = false)

        val broadcastSharedFlow = channel
            .broadcastFlow<FullGameSessionDto>(topic)
            .shareIn(this, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS))

        // Transform the raw DTO stream into a stream that includes hydrated Question data
        val questionStream: Flow<Question?> = broadcastSharedFlow
            .distinctUntilChangedBy { it.activeQuestion?.id } // Only trigger download if question ID changes
            .flatMapLatest { session ->
                session.activeQuestion?.let { getQuestionFlowHelper(sessionId, it) } ?: flowOf(null)
            }

        combine(broadcastSharedFlow, questionStream) { status, question ->
            status.toDomain(auth.currentUserId) { question }
        }.collect {
            send(it)
        }

        awaitClose {
            logger.d { "Closing game session observer for $sessionId" }
            launch {
                channel.unsubscribe()
            }
        }
    }.toResultFLow().onEach { res ->
        res.onFailure { logger.e(it) { "Error in game session flow" } }
    }

    // --- RPC Actions ---

    private suspend fun markMeAsReady(sessionId: Uuid) {
        postgrest.rpc("mark_participant_ready", mapOf("p_session_id" to sessionId))
    }

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.rpc("participant_heartbeat", mapOf("p_session_id" to sessionId))
        Unit
    }

    override suspend fun nextRound(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.rpc("next_round", mapOf("p_session_id" to sessionId))
    }

    override suspend fun nextQuestion(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.from("game_sessions")
            .update({ GameSession::currentQuestionId setTo null }) {
                filter { GameSession::id eq sessionId }
            }
    }

    override suspend fun selectQuestion(sessionId: Uuid, questionId: Uuid): Result<Unit> = runCatching {
        postgrest.rpc("pick_question", mapOf(
            "p_session_id" to sessionId,
            "p_question_id" to questionId
        ))
    }

    override suspend fun buzzIn(sessionId: Uuid): Result<Boolean> = runCatching {
        postgrest.rpc("buzz_in", mapOf("p_session_id" to sessionId)).decodeAs<Boolean>()
    }

    override suspend fun judgeAnswer(sessionId: Uuid, answerId: Uuid, isCorrect: Boolean): Result<Unit> = runCatching {
        postgrest.rpc("judge_answer", JudgeAnswerRequest(sessionId, answerId, isCorrect))
    }

    companion object {
        private const val TAG = "GameProcessRepository"
        private val MEDIA_URL_EXPIRE_DURATION = 1.hours
        private const val DOWNLOAD_MEDIA_RETRIES = 5L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
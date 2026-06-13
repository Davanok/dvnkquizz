package com.davanok.dvnkquizz.core.data.game

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.core.filesystem.div
import com.davanok.dvnkquizz.core.core.id.currentUserId
import com.davanok.dvnkquizz.core.core.result.toResultFlow
import com.davanok.dvnkquizz.core.domain.game.entities.BuzzInRequest
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameSessionDto
import com.davanok.dvnkquizz.core.domain.game.entities.GameSession
import com.davanok.dvnkquizz.core.domain.game.entities.JudgeAnswerRequest
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.game.entities.SessionRealtimeDataDto
import com.davanok.dvnkquizz.core.domain.game.repositories.GameProcessRepository
import com.davanok.dvnkquizz.core.domain.gamePackage.enums.MediaKind
import com.davanok.dvnkquizz.core.platform.Platform
import com.davanok.dvnkquizz.core.platform.currentPlatform
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameProcessRepositoryImpl(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val storage: Storage,
    private val auth: Auth,
    @Named("tempDir") tempDir: Path,
    logger: Logger
) : GameProcessRepository {
    private val tempDirectory = tempDir
    private val logger = logger.withTag(TAG)

    override val HEARTBEAT_TIMEOUT_MS: Long = 30_000

    /**
     * Downloads media file with progress reporting and local caching.
     */
    private fun downloadMediaAsFlow(question: QuestionDto): Flow<Question> = flow {
        val mediaUrl = question.mediaUrl ?: return@flow
        val extension = mediaUrl.substringAfterLast('.', "bin")
        val filename = "${question.id}_${mediaUrl.hashCode()}.$extension"

        val localPath = tempDirectory / filename
        val tmpPath = tempDirectory / "$filename.tmp"

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

    private fun getQuestionFlowHelper(sessionId: Uuid, question: QuestionDto): Flow<Question> = when {
        question.mediaKind == MediaKind.NONE || question.mediaUrl == null -> flowOf(question.toDomain())

        Platform.currentPlatform() is Platform.Web -> flow {
            val signedUrl = storage.from("questions")
                .createSignedUrl(question.mediaUrl, MEDIA_URL_EXPIRE_DURATION)

            emit(
                question.toDomain(
                    mediaUrl = signedUrl,
                    progress = 1f
                )
            )
        }
        else -> downloadMediaAsFlow(question)
    }.onCompletion { markMeAsReady(sessionId) }

    private suspend fun convertProfileImages(
        images: Map<Uuid, String?>,
        cache: MutableMap<String, String>
    ): Map<Uuid, String?> {
        val missingPaths = images.values
            .filterNotNull()
            .distinct()
            .filter { it !in cache }

        if (missingPaths.isNotEmpty()) {
            storage.from("profiles")
                .createSignedUrls(
                    PROFILE_IMAGE_URL_EXPIRES_IN,
                    missingPaths
                ).forEach { url ->
                    cache[url.path] = url.signedURL
                }
        }

        return images.mapValues { (_, path) ->
            path?.let { cache[it] }
        }
    }

    private suspend fun getSessionRealtimeData(sessionId: Uuid): SessionRealtimeDataDto? = runCatching {
        postgrest
            .from("session_realtime_data")
            .select { filter { SessionRealtimeDataDto::id eq sessionId } }
            .decodeSingleOrNull<SessionRealtimeDataDto>()
    }.getOrNull()

    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> = channelFlow {
        val topic = "game-state-update:${sessionId.toHexDashString()}"
        val channel = realtime.channel(sessionId.toString())

        channel.subscribe(blockUntilSubscribed = false)

        val initialFlow = flow {
            emit(getSessionRealtimeData(sessionId)?.data)
        }.filterNotNull()

        val realtimeFlow: Flow<FullGameSessionDto> = channel
            .broadcastFlow<FullGameSessionDto>(topic)
            .shareIn(
                scope = this,
                started = SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT.inWholeMilliseconds),
                replay = 1
            )

        val fallbackFlow: Flow<FullGameSessionDto> = realtimeFlow
            .debounce(REALTIME_FLOW_DEBOUNCE)
            .mapLatest { getSessionRealtimeData(sessionId)?.data }
            .filterNotNull()

        val sessionFlow = merge(
            initialFlow,
            realtimeFlow,
            fallbackFlow,
        )
            .distinctUntilChanged()
            .shareIn(
                scope = this,
                started = SharingStarted.WhileSubscribed(SHARE_STOP_TIMEOUT.inWholeMilliseconds),
                replay = 1
            )

        val questionStream: Flow<Question?> = sessionFlow
            .distinctUntilChangedBy { it.activeQuestion?.id }
            .flatMapLatest { session ->
                session.activeQuestion?.let { getQuestionFlowHelper(sessionId, it) } ?: flowOf(null)
            }

        val signedUrlCache = mutableMapOf<String, String>()
        combine(
            sessionFlow,
            questionStream
        ) { status, question ->
            status.toDomain(
                currentUserId = auth.currentUserId,
                convertProfileImages = { profiles ->
                    convertProfileImages(profiles, signedUrlCache)
                },
                transformActiveQuestion = { question },
            )
        }.collect { send(it) }

        awaitClose {
            logger.d { "Closing game session observer for $sessionId" }
            launch { channel.unsubscribe() }
        }
    }.toResultFlow().onEach { res ->
        res.onFailure { logger.e(it) { "Error in game session flow" } }
    }

    // --- RPC Actions ---

    private suspend fun markMeAsReady(sessionId: Uuid) {
        postgrest.rpc("mark_participant_ready", mapOf("p_session_id" to sessionId))
    }

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> = runCatching<Unit> {
        postgrest.rpc("participant_heartbeat", mapOf("p_session_id" to sessionId))
    }.onFailure {
        logger.e(it) { "failed to send heartbeat" }
    }

    override suspend fun nextRound(sessionId: Uuid): Result<Unit> = runCatching<Unit> {
        postgrest.rpc("next_round", mapOf("p_session_id" to sessionId))
    }.onFailure {
        logger.e(it) { "failed to next round" }
    }

    override suspend fun nextQuestion(sessionId: Uuid): Result<Unit> = runCatching<Unit> {
        postgrest.from("game_sessions")
            .update({ GameSession::currentQuestionId setTo null }) {
                filter { GameSession::id eq sessionId }
            }
    }.onFailure {
        logger.e(it) { "failed to next question" }
    }

    override suspend fun selectQuestion(sessionId: Uuid, questionId: Uuid): Result<Unit> = runCatching<Unit> {
        postgrest.rpc("pick_question", mapOf(
            "p_session_id" to sessionId,
            "p_question_id" to questionId
        ))
    }.onFailure {
        logger.e(it) { "failed to select question" }
    }

    override suspend fun buzzIn(sessionId: Uuid, answer: String): Result<Boolean> = runCatching {
        postgrest.rpc(
            "buzz_in",
            BuzzInRequest(sessionId, answer)
        ).decodeAs<Boolean>()
    }.onFailure {
        logger.e(it) { "failed to buzz" }
    }

    override suspend fun judgeAnswer(sessionId: Uuid, answerId: Uuid, isCorrect: Boolean): Result<Unit> = runCatching<Unit> {
        postgrest.rpc("judge_answer", JudgeAnswerRequest(sessionId, answerId, isCorrect))
    }.onFailure {
        logger.e(it) { "failed to judge answer" }
    }

    companion object {
        private const val TAG = "GameProcessRepository"
        private val MEDIA_URL_EXPIRE_DURATION = 1.hours
        private val PROFILE_IMAGE_URL_EXPIRES_IN = 5.minutes
        private const val DOWNLOAD_MEDIA_RETRIES = 5L
        private val SHARE_STOP_TIMEOUT = 5.seconds
        private val REALTIME_FLOW_DEBOUNCE = 2.seconds
    }
}
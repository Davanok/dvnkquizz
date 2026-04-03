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
import com.davanok.dvnkquizz.core.utils.currentUserId
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
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

    private suspend fun markMeAsReady(sessionId: Uuid) {
        postgrest.rpc(
            function = "mark_participant_ready",
            parameters = mapOf("p_session_id" to sessionId)
        )
    }

    private fun downloadMediaAsFlow(question: QuestionDto): Flow<Question> = flow {
        val mediaUrl = question.mediaUrl ?: return@flow
        val extension = mediaUrl.substringAfterLast('.', "bin")

        val urlHash = mediaUrl.hashCode()
        val filename = "${question.id}_$urlHash.$extension"
        val localPath = Path(SystemTemporaryDirectory, filename)

        if (SystemFileSystem.exists(localPath)) {
            emit(question.toDomain(
                mediaUrl = localPath.toString(),
                progress = 1f
            ))
            return@flow
        }

        val tmpPath = Path(SystemTemporaryDirectory, "$filename.tmp")

        runCatching {
            SystemFileSystem.sink(tmpPath).buffered().use { sink ->
                storage.from("questions")
                    .downloadAuthenticatedAsFlow(mediaUrl, channel = sink.asByteWriteChannel())
                    .collect { status ->
                        val domainQuestion = when (status) {
                            is DownloadStatus.ByteData -> error("using channel, should not receive ByteData")
                            is DownloadStatus.Progress -> question.toDomain(
                                mediaUrl = tmpPath.toString(),
                                progress = status.totalBytesReceived.toFloat() / status.contentLength.toFloat()
                            )
                            is DownloadStatus.Success -> question.toDomain(
                                mediaUrl = localPath.toString(), // При успехе отдаем финальный путь
                                progress = 1f
                            )
                        }
                        emit(domainQuestion)
                    }
            }

            SystemFileSystem.atomicMove(tmpPath, localPath)

        }.onFailure { thr ->
            if (SystemFileSystem.exists(tmpPath)) {
                SystemFileSystem.delete(tmpPath)
            }
            throw thr
        }
    }.retry(DOWNLOAD_MEDIA_RETRIES)

    private fun getQuestionFlowHelper(sessionId: Uuid, question: QuestionDto): Flow<Question> = flow {
        if (question.mediaKind == MediaKind.NONE || question.mediaUrl == null)
            emit(question.toDomain())
        else
            emitAll(downloadMediaAsFlow(question))
        markMeAsReady(sessionId)
    }

    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> = channelFlow {
        val channel = realtime.channel(sessionId.toString()) {}
        channel.subscribe(false)

        val broadcastSharedFlow = channel
            .broadcastFlow<FullGameSessionDto>("game-state-update:${sessionId.toHexDashString()}")
            .onCompletion { channel.unsubscribe() }
            .shareIn(this, SharingStarted.WhileSubscribed(5_000))

        val questionStream: Flow<Question?> = broadcastSharedFlow
            .distinctUntilChangedBy { it.activeQuestion to it.session.isAnswerVisible }
            .flatMapLatest { session ->
                if (session.activeQuestion == null) flowOf(null)
                else getQuestionFlowHelper(sessionId, session.activeQuestion)
            }

        combine(
            broadcastSharedFlow,
            questionStream
        ) { status, question ->
            status.toDomain(auth.currentUserId) { question }
        }.collect {
            send(it)
        }
    }.toResultFLow().onEach {
        logger.d { it.toString() }
    }

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> = runCatching {
        logger.d { "Sending heartbeat for session: $sessionId" }

        postgrest.rpc(
            "participant_heartbeat",
            parameters = mapOf("p_session_id" to sessionId)
        )
        Unit
    }.onFailure {
        logger.e(it) { "Heartbeat RPC failed" }
    }

    override suspend fun nextRound(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.rpc(
            function = "next_round",
            parameters = mapOf("p_session_id" to sessionId)
        )
    }

    override suspend fun nextQuestion(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.from("game_sessions")
            .update({ GameSession::currentQuestionId setTo null }) {
                filter { GameSession::id eq sessionId }
            }
    }

    override suspend fun selectQuestion(sessionId: Uuid, questionId: Uuid): Result<Unit> {
        logger.i { "selectQuestion: sessionId=$sessionId questionId=$questionId" }
        return runCatching<Unit> {
            postgrest.rpc(
                function = "pick_question",
                parameters = mapOf(
                    "p_session_id" to sessionId,
                    "p_question_id" to questionId
                )
            )
        }.onSuccess {
            logger.d { "selectQuestion success" }
        }.onFailure {
            logger.e(it) { "selectQuestion failed" }
        }
    }

    override suspend fun buzzIn(sessionId: Uuid): Result<Boolean> {
        logger.i { "buzzIn called: sessionId=$sessionId" }
        return runCatching {
            postgrest.rpc(
                function = "buzz_in",
                parameters = mapOf("p_session_id" to sessionId)
            ).decodeAs<Boolean>()
        }.onFailure {
            logger.e(it) { "buzzIn failed" }
        }
    }

    override suspend fun judgeAnswer(sessionId: Uuid, answerId: Uuid, isCorrect: Boolean): Result<Unit> {
        logger.i { "judgeAnswer: sessionId=$sessionId answerId=$answerId isCorrect=$isCorrect" }
        return runCatching<Unit> {
            postgrest.rpc(
                function = "judge_answer",
                parameters = JudgeAnswerRequest(
                    sessionId = sessionId,
                    answerId = answerId,
                    isCorrect = isCorrect
                )
            )
        }.onSuccess {
            logger.d { "judgeAnswer success" }
        }.onFailure {
            logger.e(it) { "judgeAnswer failed" }
        }
    }

    companion object {
        private const val TAG = "GameProcessRepository"
        private const val DOWNLOAD_MEDIA_RETRIES = 5L
    }
}
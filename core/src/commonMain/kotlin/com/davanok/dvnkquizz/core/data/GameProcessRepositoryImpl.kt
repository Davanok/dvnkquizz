package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.repositories.GameProcessRepository
import com.davanok.dvnkquizz.core.domain.repositories.ObserveSessionRepository
import com.davanok.dvnkquizz.core.platform.Platform
import com.davanok.dvnkquizz.core.platform.currentPlatform
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.storage.DownloadStatus
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.downloadAuthenticatedAsFlow
import io.ktor.utils.io.asByteWriteChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameProcessRepositoryImpl(
    private val postgrest: Postgrest,
    private val storage: Storage,
    logger: Logger,
    private val observeSessionRepository: ObserveSessionRepository,
) : GameProcessRepository {

    private val logger = logger.withTag(TAG)

    override val HEARTBEAT_TIMEOUT_MS: Long = observeSessionRepository.HEARTBEAT_TIMEOUT_MS

    private suspend fun markMeAsReady(sessionId: Uuid) {
        postgrest.rpc(
            function = "mark_participant_ready",
            parameters = mapOf("p_session_id" to sessionId)
        )
    }

    @OptIn(SupabaseExperimental::class)
    private fun observeSessionAnswers(sessionId: Uuid): Flow<List<SessionAnswer>> =
        postgrest.from("session_answers")
            .selectAsFlow(
                SessionAnswer::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )

    private suspend fun getSessionBoard(sessionId: Uuid, roundId: Uuid): Result<List<GameBoardItem>> {
        logger.i { "getSessionBoard: sessionId=$sessionId roundId=$roundId" }
        return runCatching {
            postgrest.rpc(
                function = "get_session_board",
                parameters = mapOf(
                    "p_session_id" to sessionId,
                    "p_round_id" to roundId
                )
            ).decodeList<GameBoardItem>()
        }.onSuccess {
            logger.d { "Board loaded: items=${it.size}" }
        }.onFailure {
            logger.e(it) { "getSessionBoard failed" }
        }
    }

    private fun getActiveQuestion(sessionId: Uuid): Flow<Question> = flow {
        logger.i { "getActiveQuestion: sessionId=$sessionId" }

        runCatching {
            val question = postgrest.rpc(
                function = "get_active_question",
                parameters = mapOf("p_session_id" to sessionId)
            ).decodeSingle<QuestionDto>()

            when {
                question.mediaKind == MediaKind.NONE || question.mediaUrl == null -> {
                    emit(question.toDomain())
                }
                Platform.currentPlatform() is Platform.Web -> {
                    val mediaUrl = storage.from("questions")
                        .createSignedUrl(question.mediaUrl, 1.minutes)
                    emit(question.toDomain(mediaUrl = mediaUrl, progress = 1f))
                }
                else -> {
                    emitAll(downloadMediaAsFlow(question))
                }
            }
        }.onFailure {
            logger.e(it) { "failed to get active question" }
        }.also {
            markMeAsReady(sessionId)
        }
    }

    private fun downloadMediaAsFlow(question: QuestionDto): Flow<Question> = flow {
        val mediaUrl = question.mediaUrl ?: return@flow
        val extension = mediaUrl.substringAfterLast('.', "bin")
        val filename = "${Uuid.random()}.$extension"
        val localPath = Path(SystemTemporaryDirectory, filename)

        SystemFileSystem.sink(localPath).buffered().use { sink ->
            storage.from("questions")
                .downloadAuthenticatedAsFlow(mediaUrl, channel = sink.asByteWriteChannel())
                .collect { status ->
                    val domainQuestion = when (status) {
                        is DownloadStatus.ByteData -> error("using channel, should not receive ByteData")
                        is DownloadStatus.Progress -> question.toDomain(
                            mediaUrl = localPath.toString(),
                            progress = status.totalBytesReceived.toFloat() / status.contentLength.toFloat()
                        )

                        is DownloadStatus.Success -> question.toDomain(
                            mediaUrl = localPath.toString(),
                            progress = 1f
                        )
                    }
                    emit(domainQuestion)
                }
        }
    }

    override fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> = flow {
        val sessionEnricher = GameSessionEnricher(
            observeActiveQuestion = { getActiveQuestion(sessionId) },
            getSessionBoard = { getSessionBoard(sessionId, it).getOrThrow() }
        )

        val resultFlow = sessionEnricher.observeEnrichedSession(
            statusFlow = observeSessionRepository.observeGameSessionStatus(sessionId).map { it.getOrThrow() },
            answersFlow = observeSessionAnswers(sessionId)
        ).toResultFLow()

        emitAll(resultFlow)
    }.onEach {
        logger.d { "observeGameSession updated: $it" }
    }

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> =
        observeSessionRepository.sendHeartbeat(sessionId)

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
                parameters = mapOf(
                    "p_session_id" to sessionId,
                    "p_answer_id" to answerId,
                    "p_is_correct" to isCorrect
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
    }
}
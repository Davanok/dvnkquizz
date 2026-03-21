package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.repositories.GameProcessRepository
import com.davanok.dvnkquizz.core.domain.repositories.ObserveSessionRepository
import com.davanok.dvnkquizz.core.utils.combineResultFlow
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameProcessRepositoryImpl(
    private val postgrest: Postgrest,
    private val logger: Logger,
    private val observeSessionRepository: ObserveSessionRepository,
): GameProcessRepository {

    @OptIn(SupabaseExperimental::class)
    private fun observeSessionAnswers(sessionId: Uuid): Flow<Result<List<SessionAnswer>>> {
        logger.i { "observeSessionAnswers started for session=$sessionId" }

        return postgrest.from("session_answers")
            .selectAsFlow(
                SessionAnswer::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )
            .onEach {
                logger.d { "Answers update received: count=${it.size}" }
            }
            .catch {
                logger.e(it) { "observeSessionAnswers flow error" }
                throw it
            }
            .toResultFLow()
    }

    private suspend fun getSessionBoard(
        sessionId: Uuid,
        roundId: Uuid
    ): Result<List<GameBoardItem>> {
        logger.i { "getSessionBoard: sessionId=$sessionId roundId=$roundId" }

        return runCatching {
            postgrest.rpc(
                "get_session_board",
                mapOf(
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
    private suspend fun getActiveQuestion(sessionId: Uuid): Result<Question> {
        logger.i { "getActiveQuestion: sessionId=$sessionId" }

        return runCatching {
            postgrest.rpc(
                "get_active_question",
                mapOf("p_session_id" to sessionId)
            ).decodeSingle<Question>()
        }.onSuccess {
            logger.d { "Active question loaded: ${it.id}" }
        }.onFailure {
            logger.e(it) { "getActiveQuestion failed" }
        }
    }

    override fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> {
        logger.i { "observeFullGameSession started: sessionId=$sessionId" }

        return combineResultFlow(
            observeSessionRepository.observeGameSessionStatus(sessionId),
            observeSessionAnswers(sessionId)
        ) { sessionStatus, answers ->
            val session = sessionStatus.session
            val participants = sessionStatus.participants

            logger.d {
                "FullGameSession update: participants=${participants.size} answers=${answers.size}"
            }

            FullGameSession(
                session = session,
                gamePackage = sessionStatus.gamePackage,
                participants = participants,
                answers = answers,
                isHost = sessionStatus.isHost,
                gameBoard = session.currentRoundId?.let { getSessionBoard(sessionId, it).getOrThrow() }.orEmpty(),
                activeQuestion = session.currentQuestionId?.let { getActiveQuestion(sessionId).getOrThrow() }
            )
        }
    }

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> =
        observeSessionRepository.sendHeartbeat(sessionId)

    override suspend fun nextRound(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.rpc(
            "next_round",
            mapOf("p_session_id" to sessionId)
        )
    }

    override suspend fun nextQuestion(sessionId: Uuid): Result<Unit> = runCatching {
        postgrest.from("game_sessions")
            .update({
                GameSession::currentQuestionId setTo null
            }) {
                filter { GameSession::id eq sessionId }
            }
    }

    override suspend fun selectQuestion(
        sessionId: Uuid,
        questionId: Uuid
    ): Result<Unit> {
        logger.i { "pickQuestion: sessionId=$sessionId questionId=$questionId" }

        return runCatching {
            postgrest.rpc(
                "pick_question",
                mapOf(
                    "p_session_id" to sessionId,
                    "p_question_id" to questionId
                )
            )
            Unit
        }.onSuccess {
            logger.d { "pickQuestion success" }
        }.onFailure {
            logger.e(it) { "pickQuestion failed" }
        }
    }

    override suspend fun buzzIn(sessionId: Uuid): Result<Unit> {
        logger.i { "buzzIn called: sessionId=$sessionId" }

        return runCatching {
            postgrest.rpc(
                "buzz_in",
                mapOf("p_session_id" to sessionId)
            )
            Unit
        }.onSuccess {
            logger.i { "buzzIn success: participantId=$it" }
        }.onFailure {
            logger.e(it) { "buzzIn failed" }
        }
    }


    override suspend fun judgeAnswer(
        sessionId: Uuid,
        participantId: Uuid,
        isCorrect: Boolean
    ): Result<Unit> {
        logger.i {
            "judgeAnswer: sessionId=$sessionId participantId=$participantId isCorrect=$isCorrect"
        }

        return runCatching {
            postgrest.rpc(
                "judge_answer",
                mapOf(
                    "p_session_id" to sessionId,
                    "p_participant_id" to participantId,
                    "p_is_correct" to isCorrect
                )
            )
            Unit
        }.onSuccess {
            logger.d { "judgeAnswer success" }
        }.onFailure {
            logger.e(it) { "judgeAnswer failed" }
        }
    }

    override val HEARTBEAT_TIMEOUT_MS: Long = observeSessionRepository.HEARTBEAT_TIMEOUT_MS
}
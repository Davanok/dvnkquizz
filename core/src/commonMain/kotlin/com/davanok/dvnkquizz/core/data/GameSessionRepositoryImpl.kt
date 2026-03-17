package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.CreateSessionRequest
import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionRequest
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.ParticipantDto
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.utils.combineResultFlow
import com.davanok.dvnkquizz.core.utils.currentUserId
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameSessionRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val log: Logger
) : GameSessionRepository {

    // --- Session Management ---

    override suspend fun createSession(
        packageId: Uuid,
        nickname: String
    ): Result<CreateSessionResponse> {
        log.i { "createSession called: packageId=$packageId nickname=$nickname" }

        return runCatching {
            postgrest.rpc(
                function = "create_game_session",
                parameters = CreateSessionRequest(packageId, nickname)
            ).decodeSingle<CreateSessionResponse>()
        }.onSuccess {
            log.i { "createSession success: inviteCode=${it.inviteCode}" }
        }.onFailure {
            log.e(it) { "createSession failed" }
        }
    }

    override suspend fun joinSession(
        inviteCode: String,
        nickname: String
    ): Result<JoinSessionResponse> {
        log.i { "joinSession called: inviteCode=$inviteCode nickname=$nickname" }

        return runCatching {
            postgrest.rpc(
                function = "join_game_session",
                parameters = JoinSessionRequest(inviteCode, nickname)
            ).decodeSingle<JoinSessionResponse>()
        }.onSuccess {
            log.i { "joinSession success: sessionId=${it.sessionId}" }
        }.onFailure {
            log.e(it) { "joinSession failed" }
        }
    }

    override suspend fun updateSessionStatus(
        sessionId: Uuid,
        newStatus: SessionStatus
    ): Result<Unit> {
        log.i { "updateSessionStatus: sessionId=$sessionId newStatus=$newStatus" }

        return runCatching {
            postgrest.from("game_sessions")
                .update(mapOf("status" to newStatus)) {
                    filter { eq("id", sessionId) }
                }
            Unit
        }.onSuccess {
            log.d { "Session status updated successfully" }
        }.onFailure {
            log.e(it) { "updateSessionStatus failed" }
        }
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeParticipants(sessionId: Uuid): Flow<Result<List<Participant>>> {
        log.i { "observeParticipants started for session=$sessionId" }

        return postgrest.from("participants")
            .selectAsFlow<ParticipantDto, Uuid>(
                ParticipantDto::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )
            .map { participants ->
                log.d { "Participants update received: count=${participants.size}" }

                participants.map {
                    it.toDomain(auth.currentUserId)
                }
            }
            .onStart {
                log.d { "observeParticipants flow started" }
            }
            .catch {
                log.e(it) { "observeParticipants flow error" }
                throw it
            }
            .toResultFLow()
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeSession(sessionId: Uuid): Flow<Result<GameSession>> {
        log.i { "observeSession started for session=$sessionId" }

        return postgrest.from("game_sessions")
            .selectSingleValueAsFlow(
                GameSession::id
            ) { GameSession::id eq sessionId }
            .onEach {
                log.d { "Session update received: $it" }
            }
            .catch {
                log.e(it) { "observeSession flow error" }
                throw it
            }
            .toResultFLow()
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeSessionAnswers(sessionId: Uuid): Flow<Result<List<SessionAnswer>>> {
        log.i { "observeSessionAnswers started for session=$sessionId" }

        return postgrest.from("session_answers")
            .selectAsFlow(
                SessionAnswer::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )
            .onEach {
                log.d { "Answers update received: count=${it.size}" }
            }
            .catch {
                log.e(it) { "observeSessionAnswers flow error" }
                throw it
            }
            .toResultFLow()
    }

    override suspend fun pickQuestion(
        sessionId: Uuid,
        questionId: Uuid
    ): Result<Unit> {
        log.i { "pickQuestion: sessionId=$sessionId questionId=$questionId" }

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
            log.d { "pickQuestion success" }
        }.onFailure {
            log.e(it) { "pickQuestion failed" }
        }
    }

    override suspend fun buzzIn(sessionId: Uuid): Result<Uuid> {
        log.i { "buzzIn called: sessionId=$sessionId" }

        return runCatching {
            postgrest.rpc(
                "buzz_in",
                mapOf("p_session_id" to sessionId)
            ).decodeAs<Uuid>()
        }.onSuccess {
            log.i { "buzzIn success: participantId=$it" }
        }.onFailure {
            log.e(it) { "buzzIn failed" }
        }
    }

    override suspend fun judgeAnswer(
        sessionId: Uuid,
        participantId: Uuid,
        isCorrect: Boolean
    ): Result<Unit> {
        log.i {
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
            log.d { "judgeAnswer success" }
        }.onFailure {
            log.e(it) { "judgeAnswer failed" }
        }
    }

    override suspend fun getSessionBoard(
        sessionId: Uuid,
        roundId: Uuid
    ): Result<List<GameBoardItem>> {
        log.i { "getSessionBoard: sessionId=$sessionId roundId=$roundId" }

        return runCatching {
            postgrest.rpc(
                "get_session_board",
                mapOf(
                    "p_session_id" to sessionId,
                    "p_round_id" to roundId
                )
            ).decodeList<GameBoardItem>()
        }.onSuccess {
            log.d { "Board loaded: items=${it.size}" }
        }.onFailure {
            log.e(it) { "getSessionBoard failed" }
        }
    }

    override suspend fun getActiveQuestion(sessionId: Uuid): Result<Question> {
        log.i { "getActiveQuestion: sessionId=$sessionId" }

        return runCatching {
            postgrest.rpc(
                "get_active_question",
                mapOf("p_session_id" to sessionId)
            ).decodeSingle<Question>()
        }.onSuccess {
            log.d { "Active question loaded: ${it.id}" }
        }.onFailure {
            log.e(it) { "getActiveQuestion failed" }
        }
    }

    override suspend fun skipQuestion(sessionId: Uuid): Result<Unit> {
        log.i { "skipQuestion: sessionId=$sessionId" }

        return runCatching {
            postgrest.from("game_sessions")
                .update(mapOf("current_question_id" to null)) {
                    filter { eq("id", sessionId) }
                }
            Unit
        }.onSuccess {
            log.d { "Question skipped" }
        }.onFailure {
            log.e(it) { "skipQuestion failed" }
        }
    }

    override fun observeFullGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> {
        log.i { "observeFullGameSession started: sessionId=$sessionId" }

        return combineResultFlow(
            observeSession(sessionId),
            observeParticipants(sessionId),
            observeSessionAnswers(sessionId)
        ) { session, participants, answers ->
            log.d {
                "FullGameSession update: participants=${participants.size} answers=${answers.size}"
            }

            FullGameSession(
                session = session,
                participants = participants,
                answers = answers,
                isHost = session.hostId == auth.currentUserId
            )
        }
    }

    override fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>> {
        log.i { "observeFullGameSession started: sessionId=$sessionId" }

        return combineResultFlow(
            observeSession(sessionId),
            observeParticipants(sessionId)
        ) { session, participants ->

            log.d {
                "FullGameSession update: participants=${participants.size}"
            }

            GameSessionStatus(
                session = session,
                participants = participants,
                isHost = session.hostId == auth.currentUserId
            )
        }
    }
}
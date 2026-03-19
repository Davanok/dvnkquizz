package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.ParticipantDto
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.entities.UserProfile
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
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameSessionRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val logger: Logger
) : GameSessionRepository {

    // --- Session Management ---

    override suspend fun createSession(
        packageId: Uuid
    ): Result<CreateSessionResponse> {
        logger.i { "createSession called: packageId=$packageId" }

        return runCatching {
            postgrest.rpc(
                function = "create_game_session",
                parameters = mapOf("p_package_id" to packageId)
            ).decodeSingle<CreateSessionResponse>()
        }.onSuccess {
            logger.i { "createSession success: inviteCode=${it.inviteCode}" }
        }.onFailure {
            logger.e(it) { "createSession failed" }
        }
    }

    override suspend fun joinSession(
        inviteCode: String
    ): Result<JoinSessionResponse> {
        logger.i { "joinSession called: inviteCode=$inviteCode" }

        return runCatching {
            postgrest.rpc(
                function = "join_game_session",
                parameters = mapOf("p_invite_code" to inviteCode)
            ).decodeSingle<JoinSessionResponse>()
        }.onSuccess {
            logger.i { "joinSession success: sessionId=${it.sessionId}" }
        }.onFailure {
            logger.e(it) { "joinSession failed" }
        }
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

    override suspend fun updateSessionStatus(
        sessionId: Uuid,
        newStatus: SessionStatus
    ): Result<Unit> {
        logger.i { "updateSessionStatus: sessionId=$sessionId newStatus=$newStatus" }

        return runCatching {
            postgrest.from("game_sessions")
                .update({
                    GameSession::status setTo newStatus
                }) {
                    filter { eq("id", sessionId) }
                }
            Unit
        }.onSuccess {
            logger.d { "Session status updated successfully" }
        }.onFailure {
            logger.e(it) { "updateSessionStatus failed" }
        }
    }

    private suspend fun getUser(userId: Uuid): UserProfile =
        postgrest.from("users").select {
            filter {
                UserProfile::id eq userId
            }
        }.decodeSingle()

    @OptIn(SupabaseExperimental::class)
    override fun observeParticipants(sessionId: Uuid): Flow<Result<List<Participant>>> {
        logger.i { "observeParticipants started for session=$sessionId" }

        val cachedUsers = mutableMapOf<Uuid, UserProfile>()

        return postgrest.from("participants")
            .selectAsFlow<ParticipantDto, Uuid>(
                ParticipantDto::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )
            .map { participants ->
                logger.d { "Participants update received: count=${participants.size}" }

                participants.map { participant ->
                    participant.toDomain(
                        currentUserId = auth.currentUserId,
                        user = cachedUsers.getOrPut(participant.id) { getUser(participant.id) }
                    )
                }
            }
            .catch {
                logger.e(it) { "observeParticipants flow error" }
                throw it
            }
            .toResultFLow()
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeSession(sessionId: Uuid): Flow<Result<GameSession>> {
        logger.i { "observeSession started for session=$sessionId" }

        return postgrest.from("game_sessions")
            .selectSingleValueAsFlow(
                GameSession::id
            ) { GameSession::id eq sessionId }
            .onEach {
                logger.d { "Session update received: $it" }
            }
            .catch {
                logger.e(it) { "observeSession flow error" }
                throw it
            }
            .toResultFLow()
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeSessionAnswers(sessionId: Uuid): Flow<Result<List<SessionAnswer>>> {
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

    override suspend fun pickQuestion(
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

    override suspend fun buzzIn(sessionId: Uuid): Result<Uuid> {
        logger.i { "buzzIn called: sessionId=$sessionId" }

        return runCatching {
            postgrest.rpc(
                "buzz_in",
                mapOf("p_session_id" to sessionId)
            ).decodeAs<Uuid>()
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

    override suspend fun getSessionBoard(
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

    override suspend fun getActiveQuestion(sessionId: Uuid): Result<Question> {
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

    override suspend fun skipQuestion(sessionId: Uuid): Result<Unit> {
        logger.i { "skipQuestion: sessionId=$sessionId" }

        return runCatching {
            postgrest.from("game_sessions")
                .update(mapOf("current_question_id" to null)) {
                    filter { eq("id", sessionId) }
                }
            Unit
        }.onSuccess {
            logger.d { "Question skipped" }
        }.onFailure {
            logger.e(it) { "skipQuestion failed" }
        }
    }

    override fun observeFullGameSession(sessionId: Uuid): Flow<Result<FullGameSession>> {
        logger.i { "observeFullGameSession started: sessionId=$sessionId" }

        return combineResultFlow(
            observeSession(sessionId),
            observeParticipants(sessionId),
            observeSessionAnswers(sessionId)
        ) { session, participants, answers ->
            logger.d {
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
        logger.i { "observeFullGameSession started: sessionId=$sessionId" }

        return combineResultFlow(
            observeSession(sessionId),
            observeParticipants(sessionId)
        ) { session, participants ->

            logger.d {
                "FullGameSession update: participants=${participants.size}"
            }

            GameSessionStatus(
                session = session,
                participants = participants,
                isHost = session.hostId == auth.currentUserId
            )
        }
    }

    override val HEARTBEAT_TIMEOUT: Long = 30_000
}
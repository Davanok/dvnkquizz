package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionRequest
import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.GameSessionDto
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionRequest
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.utils.currentUserId
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
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameSessionRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest
): GameSessionRepository {

    // --- Session Management ---

    override suspend fun createSession(packageId: Uuid, nickname: String): CreateSessionResponse {
        return postgrest.rpc(
            function = "create_game_session",
            parameters = CreateSessionRequest(packageId, nickname)
        ).decodeSingle<CreateSessionResponse>()
    }

    override suspend fun joinSession(inviteCode: String, nickname: String): JoinSessionResponse {
        return postgrest.rpc(
            function = "join_game_session",
            parameters = JoinSessionRequest(inviteCode, nickname)
        ).decodeSingle<JoinSessionResponse>()
    }

    override suspend fun getSessionByInviteCode(inviteCode: String): GameSession? =
        postgrest.from("game_sessions")
            .select {
                filter { eq("invite_code", inviteCode.uppercase()) }
            }
            .decodeSingleOrNull<GameSessionDto>()
            ?.toDomain(auth.currentUserId)

    // --- Game Actions ---

    override suspend fun updateParticipantScore(participantId: Uuid, newScore: Int) {
        // Remember: Our RLS policy ensures only the host can successfully run this!
        postgrest.from("participants")
            .update(mapOf("score" to newScore)) {
                filter { eq("id", participantId) }
            }
    }

    override suspend fun updateSessionStatus(sessionId: Uuid, newStatus: SessionStatus) {
        postgrest.from("game_sessions")
            .update(mapOf("status" to newStatus)) {
                filter { eq("id", sessionId) }
            }
    }

    // --- RPC Calls (The Buzzer) ---

    override suspend fun pressBuzzer(sessionId: Uuid): Boolean {
        // Calls the Postgres function we created earlier to prevent race conditions
        return postgrest.rpc(
            function = "press_buzzer",
            parameters = mapOf("target_session_id" to sessionId)
        ).decodeAs<Boolean>()
    }

    @OptIn(SupabaseExperimental::class)
    override fun observeParticipants(sessionId: Uuid): Flow<List<Participant>> {
        return postgrest.from("participants")
            .selectAsFlow(
                Participant::id,
                filter = FilterOperation("session_id", FilterOperator.EQ, sessionId)
            )

    }

    @OptIn(SupabaseExperimental::class)
    override fun observeSession(sessionId: Uuid): Flow<GameSession> {
        return postgrest.from("game_sessions")
            .selectSingleValueAsFlow(
                GameSession::id
            ) { GameSession::id eq sessionId }
    }
}
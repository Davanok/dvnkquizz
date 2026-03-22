package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.domain.repositories.ObserveSessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameSessionRepositoryImpl(
    private val postgrest: Postgrest,
    logger: Logger,
    private val observeSessionRepository: ObserveSessionRepository
) : GameSessionRepository {
    private val logger = logger.withTag(TAG)

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

    override suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit> =
        observeSessionRepository.sendHeartbeat(sessionId)

    override suspend fun updateSessionStatus(
        sessionId: Uuid,
        newStatus: SessionStatus
    ): Result<Unit> {
        logger.i { "updateSessionStatus: sessionId=$sessionId newStatus=$newStatus" }

        return runCatching {
            postgrest.from("game_sessions")
                .update(mapOf("status" to newStatus.name)) {
                    filter { eq("id", sessionId) }
                }
            Unit
        }.onSuccess {
            logger.d { "Session status updated successfully" }
        }.onFailure {
            logger.e(it) { "updateSessionStatus failed" }
        }
    }

    override fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>> =
        observeSessionRepository.observeGameSessionStatus(sessionId)

    override val HEARTBEAT_TIMEOUT_MS: Long = 30_000

    companion object {
        private const val TAG = "GameSessionRepository"
    }
}
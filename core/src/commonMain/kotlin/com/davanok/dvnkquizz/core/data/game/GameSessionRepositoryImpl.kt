package com.davanok.dvnkquizz.core.data.game

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.game.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.game.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.game.repositories.GameSessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class GameSessionRepositoryImpl(
    private val postgrest: Postgrest,
    logger: Logger,
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

    override val HEARTBEAT_TIMEOUT_MS: Long = 30_000

    companion object {
        private const val TAG = "GameSessionRepository"
    }
}
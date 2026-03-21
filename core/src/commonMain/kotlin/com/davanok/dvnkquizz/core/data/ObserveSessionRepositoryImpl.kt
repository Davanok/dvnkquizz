package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.ParticipantDto
import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.entities.UserProfileDto
import com.davanok.dvnkquizz.core.domain.repositories.ObserveSessionRepository
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
class ObserveSessionRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val filesSource: FilesSource,
    private val logger: Logger
): ObserveSessionRepository {
    private suspend fun getGamePackage(packageId: Uuid): GamePackage =
        postgrest.from("game_packages")
            .select {
                filter { GamePackage::id eq packageId }
            }.decodeSingle()

    private suspend fun getUser(userId: Uuid): UserProfile {
        val profileDto = postgrest.from("users").select {
            filter { UserProfileDto::id eq userId }
        }.decodeSingle<UserProfileDto>()

        val image = profileDto.image?.let { image ->
            filesSource.fetchSource("profiles", image).getOrNull()
        }

        return profileDto.toDomain(image = image)
    }

    @OptIn(SupabaseExperimental::class)
    private fun observeParticipants(sessionId: Uuid): Flow<Result<List<Participant>>> {
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
    private fun observeSession(sessionId: Uuid): Flow<Result<GameSession>> {
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

    override fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>> {
        logger.i { "observeFullGameSession started: sessionId=$sessionId" }

        var gamePackage: GamePackage? = null
        return combineResultFlow(
            observeSession(sessionId),
            observeParticipants(sessionId)
        ) { session, participants ->
            logger.d {
                "FullGameSession update: participants=${participants.size}"
            }

            if (gamePackage == null)
                gamePackage = getGamePackage(session.packageId)

            GameSessionStatus(
                session = session,
                gamePackage = gamePackage,
                participants = participants,
                isHost = session.hostId == auth.currentUserId
            )
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


    override val HEARTBEAT_TIMEOUT_MS: Long = 30_000
}
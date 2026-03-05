package com.davanok.dvnkquizz.core.domain.usecases

import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.uuid.Uuid

@Inject
class GetLobbyDataUseCase(
    private val repository: GameSessionRepository
) {
    data class LobbyData(
        val session: GameSession,
        val participants: List<Participant>
    )

    fun observeLobbyData(sessionId: Uuid): Flow<LobbyData> = combine(
        repository.observeSession(sessionId),
        repository.observeParticipants(sessionId)
    ) { session, participants ->
        LobbyData(session, participants)
    }
}

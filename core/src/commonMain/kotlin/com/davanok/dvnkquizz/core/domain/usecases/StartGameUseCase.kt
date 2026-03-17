package com.davanok.dvnkquizz.core.domain.usecases

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

@Inject
class StartGameUseCase(private val repository: GameSessionRepository) {
    suspend fun createGame(packageId: Uuid, nickname: String): Result<CreateSessionResponse> =
        repository.createSession(packageId, nickname)

    suspend fun joinGame(inviteCode: String, nickname: String): Result<Uuid> =
        repository.joinSession(inviteCode, nickname).map { it.sessionId }
}
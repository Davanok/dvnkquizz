package com.davanok.dvnkquizz.core.domain.usecases

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

@Inject
class HostGameUseCase(private val repository: GameSessionRepository) {
    suspend fun createSession(packageId: Uuid, nickname: String): Result<CreateSessionResponse> =
        runCatching {
            repository.createSession(packageId, nickname)
        }
}
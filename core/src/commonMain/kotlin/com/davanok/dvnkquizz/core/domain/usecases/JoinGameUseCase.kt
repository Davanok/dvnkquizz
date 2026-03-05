package com.davanok.dvnkquizz.core.domain.usecases

import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.Inject
import kotlin.uuid.Uuid

@Inject
class JoinGameUseCase(private val repository: GameSessionRepository) {
    suspend fun joinGame(inviteCode: String, nickname: String): Result<Uuid> =
        runCatching {
            repository.joinSession(inviteCode, nickname).sessionId
        }
}
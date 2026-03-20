package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface ObserveSessionRepository {
    fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>>

    suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit>

    val HEARTBEAT_TIMEOUT_MS: Long
}
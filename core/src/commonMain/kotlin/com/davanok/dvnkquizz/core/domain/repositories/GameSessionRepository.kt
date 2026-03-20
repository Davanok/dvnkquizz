package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GameSessionRepository {
    suspend fun createSession(packageId: Uuid): Result<CreateSessionResponse>
    suspend fun joinSession(inviteCode: String): Result<JoinSessionResponse>

    suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit>
    suspend fun updateSessionStatus(sessionId: Uuid, newStatus: SessionStatus): Result<Unit>

    fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>>

    val HEARTBEAT_TIMEOUT_MS: Long
}
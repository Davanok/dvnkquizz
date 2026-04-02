package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import kotlin.uuid.Uuid

interface GameSessionRepository {
    suspend fun createSession(packageId: Uuid): Result<CreateSessionResponse>
    suspend fun joinSession(inviteCode: String): Result<JoinSessionResponse>

    val HEARTBEAT_TIMEOUT_MS: Long
}
package com.davanok.dvnkquizz.core.domain.game.repositories

import com.davanok.dvnkquizz.core.domain.game.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.game.entities.JoinSessionResponse
import kotlin.uuid.Uuid

interface GameSessionRepository {
    suspend fun createSession(packageId: Uuid): Result<CreateSessionResponse>
    suspend fun joinSession(inviteCode: String): Result<JoinSessionResponse>

    val HEARTBEAT_TIMEOUT_MS: Long
}
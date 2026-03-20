package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GameProcessRepository {
    fun observeGameSession(sessionId: Uuid): Flow<Result<FullGameSession>>
    suspend fun sendHeartbeat(sessionId: Uuid): Result<Unit>


    suspend fun nextRound(sessionId: Uuid): Result<Unit>
    suspend fun nextQuestion(sessionId: Uuid): Result<Unit>

    suspend fun selectQuestion(sessionId: Uuid, questionId: Uuid): Result<Unit>

    suspend fun buzzIn(sessionId: Uuid): Result<Unit>

    suspend fun judgeAnswer(sessionId: Uuid, participantId: Uuid, isCorrect: Boolean): Result<Unit>

    val HEARTBEAT_TIMEOUT_MS: Long
}
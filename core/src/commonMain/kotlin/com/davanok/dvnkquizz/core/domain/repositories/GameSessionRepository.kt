package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GameSessionRepository {
    suspend fun createSession(packageId: Uuid, nickname: String): Result<CreateSessionResponse>
    suspend fun joinSession(inviteCode: String, nickname: String): Result<JoinSessionResponse>

    suspend fun updateSessionStatus(sessionId: Uuid, newStatus: SessionStatus): Result<Unit>

    fun observeParticipants(sessionId: Uuid): Flow<Result<List<Participant>>>
    fun observeSession(sessionId: Uuid): Flow<Result<GameSession>>
    fun observeSessionAnswers(sessionId: Uuid): Flow<Result<List<SessionAnswer>>>

    suspend fun pickQuestion(sessionId: Uuid, questionId: Uuid): Result<Unit>
    suspend fun buzzIn(sessionId: Uuid): Result<Uuid>
    suspend fun judgeAnswer(sessionId: Uuid, participantId: Uuid, isCorrect: Boolean): Result<Unit>
    suspend fun getSessionBoard(sessionId: Uuid, roundId: Uuid): Result<List<GameBoardItem>>
    suspend fun getActiveQuestion(sessionId: Uuid): Result<Question>
    suspend fun skipQuestion(sessionId: Uuid): Result<Unit>

    fun observeFullGameSession(sessionId: Uuid): Flow<Result<FullGameSession>>
    fun observeGameSessionStatus(sessionId: Uuid): Flow<Result<GameSessionStatus>>
}
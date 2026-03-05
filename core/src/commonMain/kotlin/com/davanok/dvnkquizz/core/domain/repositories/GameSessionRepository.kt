package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.CreateSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.JoinSessionResponse
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GameSessionRepository {
    suspend fun createSession(packageId: Uuid, nickname: String): CreateSessionResponse
    suspend fun joinSession(inviteCode: String, nickname: String): JoinSessionResponse
    suspend fun getSessionByInviteCode(inviteCode: String): GameSession?
    suspend fun updateParticipantScore(participantId: Uuid, newScore: Int)
    suspend fun updateSessionStatus(sessionId: Uuid, newStatus: SessionStatus)
    suspend fun pressBuzzer(sessionId: Uuid): Boolean
    fun observeParticipants(sessionId: Uuid): Flow<List<Participant>>
    fun observeSession(sessionId: Uuid): Flow<GameSession>
}
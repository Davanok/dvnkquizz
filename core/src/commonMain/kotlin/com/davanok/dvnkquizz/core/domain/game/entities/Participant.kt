package com.davanok.dvnkquizz.core.domain.game.entities

import com.davanok.dvnkquizz.core.domain.auth.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.game.enums.ParticipantRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class ParticipantDto(
    val id: Uuid,
    @SerialName("session_id")
    val sessionId: Uuid,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    @SerialName("joined_at")
    val joinedAt: Instant,
    @SerialName("last_active_at")
    val lastActiveAt: Instant,
    @SerialName("is_ready")
    val isReady: Boolean,
    val user: UserProfile
) {
    fun toDomain(currentUserId: Uuid?) = Participant(
        id = id,
        sessionId = sessionId,
        user = user,
        score = score,
        role = role,
        joinedAt = joinedAt,
        isMe = id == currentUserId,
        isReady = isReady
    )
}
data class Participant(
    val id: Uuid,
    val sessionId: Uuid,
    val user: UserProfile,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    val joinedAt: Instant,
    val isMe: Boolean,
    val isReady: Boolean
)
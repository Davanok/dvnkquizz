package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.ParticipantRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class ParticipantDto(
    val id: Uuid,
    @SerialName("session_id") val sessionId: Uuid,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    @SerialName("joined_at") val joinedAt: String
) {
    fun toDomain(currentUserId: Uuid?, user: UserProfile) = Participant(
        id = id,
        sessionId = sessionId,
        user = user,
        score = score,
        role = role,
        joinedAt = joinedAt,
        isMe = id == currentUserId
    )
}
data class Participant(
    val id: Uuid,
    val sessionId: Uuid,
    val user: UserProfile,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    val joinedAt: String,
    val isMe: Boolean
)
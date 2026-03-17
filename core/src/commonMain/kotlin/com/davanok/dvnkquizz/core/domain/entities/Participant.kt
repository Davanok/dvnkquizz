package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.ParticipantRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class ParticipantDto(
    val id: Uuid = Uuid.random(),
    @SerialName("session_id") val sessionId: Uuid,
    @SerialName("user_id") val userId: Uuid,
    val nickname: String,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    @SerialName("joined_at") val joinedAt: String? = null
) {
    fun toDomain(currentUserId: Uuid?) = Participant(
        id = id,
        sessionId = sessionId,
        userId = userId,
        nickname = nickname,
        score = score,
        role = role,
        joinedAt = joinedAt,
        isMe = id == currentUserId
    )
}
@Serializable
data class Participant(
    val id: Uuid = Uuid.random(),
    @SerialName("session_id") val sessionId: Uuid,
    @SerialName("user_id") val userId: Uuid,
    val nickname: String,
    val score: Int = 0,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    @SerialName("joined_at") val joinedAt: String? = null,
    val isMe: Boolean
)
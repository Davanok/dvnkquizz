package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class GameSessionDto(
    val id: Uuid,
    @SerialName("package_id") val packageId: Uuid,
    @SerialName("host_id") val hostId: Uuid,
    val status: SessionStatus = SessionStatus.LOBBY,
    @SerialName("current_round_id") val currentRoundId: Uuid? = null,
    @SerialName("invite_code") val inviteCode: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_answer_visible") val isAnswerVisible: Boolean = false,
    @SerialName("current_question_id") val currentQuestionId: Uuid? = null
) {
    fun toDomain(currentUserId: Uuid?) = GameSession(
        id = id,
        packageId = packageId,
        hostId = hostId,
        status = status,
        currentRoundId = currentRoundId,
        inviteCode = inviteCode,
        createdAt = createdAt,
        isAnswerVisible = isAnswerVisible,
        currentQuestionId = currentQuestionId,
        isHost = hostId == currentUserId
    )
}

data class GameSession(
    val id: Uuid,
    val packageId: Uuid,
    val hostId: Uuid,
    val status: SessionStatus,
    val currentRoundId: Uuid?,
    val inviteCode: String?,
    val createdAt: String?,
    val isAnswerVisible: Boolean,
    val currentQuestionId: Uuid?,
    val isHost: Boolean
)
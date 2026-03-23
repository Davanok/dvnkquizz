package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class GameSession(
    val id: Uuid,
    @SerialName("package_id") val packageId: Uuid,
    @SerialName("host_id") val hostId: Uuid,
    val status: SessionStatus = SessionStatus.LOBBY,
    @SerialName("current_round_id") val currentRoundId: Uuid? = null,
    @SerialName("invite_code") val inviteCode: String? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("is_answer_visible") val isAnswerVisible: Boolean = false,
    @SerialName("current_question_id") val currentQuestionId: Uuid? = null,
    @SerialName("show_question_at") val showQuestionAt: Instant?
)
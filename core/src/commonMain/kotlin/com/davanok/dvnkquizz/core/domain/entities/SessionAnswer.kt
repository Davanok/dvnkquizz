package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SessionAnswer(
    val id: Uuid,
    @SerialName("session_id")
    val sessionId: Uuid,
    @SerialName("question_id")
    val questionId: Uuid,
    @SerialName("participant_id")
    val participantId: Uuid,
    @SerialName("is_correct")
    val isCorrect: Boolean?,
    @SerialName("points_awarded")
    val pointsAwarded: Int,
    @SerialName("answered_at")
    val answeredAt: String
)
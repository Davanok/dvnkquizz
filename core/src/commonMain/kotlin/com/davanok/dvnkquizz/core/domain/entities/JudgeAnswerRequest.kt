package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class JudgeAnswerRequest(
    @SerialName("p_session_id")
    val sessionId: Uuid,
    @SerialName("p_answer_id")
    val answerId: Uuid,
    @SerialName("p_is_correct")
    val isCorrect: Boolean
)

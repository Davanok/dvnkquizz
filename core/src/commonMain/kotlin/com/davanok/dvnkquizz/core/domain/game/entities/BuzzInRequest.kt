package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class BuzzInRequest(
    @SerialName("p_session_id")
    val sessionId: Uuid,
    @SerialName("p_answer")
    val answer: String
)

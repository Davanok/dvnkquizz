package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class GameSessionStateDto(
    @SerialName("session_id")
    val sessionId: Uuid,
    val payload: FullGameSessionDto,
    @SerialName("updated_at")
    val updatedAt: Instant
)

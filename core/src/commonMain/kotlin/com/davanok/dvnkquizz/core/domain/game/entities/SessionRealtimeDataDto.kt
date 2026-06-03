package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class SessionRealtimeDataDto(
    val id: Uuid,
    @SerialName("updated_at")
    val updatedAt: Instant,
    val data: FullGameSessionDto
)
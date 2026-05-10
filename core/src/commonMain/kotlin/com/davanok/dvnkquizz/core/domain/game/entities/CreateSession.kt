package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateSessionResponse(
    @SerialName("o_session_id") val sessionId: Uuid,
    @SerialName("o_invite_code") val inviteCode: String
)
package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateSessionRequest(
    @SerialName("p_package_id") val packageId: Uuid,
    @SerialName("p_nickname") val nickname: String
)

@Serializable
data class CreateSessionResponse(
    @SerialName("session_id") val sessionId: Uuid,
    @SerialName("invite_code") val inviteCode: String
)
package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.ParticipantRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@Serializable
data class JoinSessionRequest(
    @SerialName("p_invite_code") val inviteCode: String,
    @SerialName("p_nickname") val nickname: String
)

@Serializable
data class JoinSessionResponse(
    @SerialName("session_id") val sessionId: Uuid,
    @SerialName("participant_id") val participantId: Uuid,
    @SerialName("assigned_role") val assignedRole: ParticipantRole
)
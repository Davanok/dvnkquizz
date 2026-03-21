package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class UserProfileDto(
    val id: Uuid,
    val nickname: String,
    val image: String?
) {
    fun toDomain(image: ExternalFile?) = UserProfile(
        nickname = nickname,
        image = image
    )
}

data class UserProfile(
    val nickname: String,
    val image: ExternalFile?
)

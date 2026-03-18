package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserProfile(
    val id: Uuid,
    val nickname: String,
    val image: Uuid?
)

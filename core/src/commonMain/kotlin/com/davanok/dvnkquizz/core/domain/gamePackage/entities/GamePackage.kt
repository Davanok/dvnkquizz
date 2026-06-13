package com.davanok.dvnkquizz.core.domain.gamePackage.entities

import com.davanok.dvnkquizz.core.domain.auth.entities.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class GamePackageDto(
    val id: Uuid,
    @SerialName("created_at")
    val createdAt: Instant = Clock.System.now(),
    @SerialName("updated_at")
    val updatedAt: Instant = Clock.System.now(),
    val title: String,
    val description: String,
    @SerialName("author_id")
    val authorId: Uuid? = null,
    val difficulty: Int = 1,
    @SerialName("is_public")
    val isPublic: Boolean = false
)

@Serializable
data class GamePackage(
    val id: Uuid,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
    val title: String,
    val description: String,
    @SerialName("author_id")
    val authorId: Uuid? = null,
    val difficulty: Int = 1,
    @SerialName("is_public")
    val isPublic: Boolean = false,

    val author: UserProfile?
)
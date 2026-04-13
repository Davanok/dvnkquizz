package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class GamePackageDto(
    val id: Uuid,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    val title: String,
    val description: String,
    @SerialName("author_id")
    val authorId: Uuid? = null,
    val difficulty: Int = 1,
    @SerialName("is_public")
    val isPublic: Boolean = false,
)

@Serializable
data class GamePackage(
    val id: Uuid,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    val title: String,
    val description: String,
    @SerialName("author_id")
    val authorId: Uuid? = null,
    val difficulty: Int = 1,
    @SerialName("is_public")
    val isPublic: Boolean = false,

    val author: UserProfile?
)
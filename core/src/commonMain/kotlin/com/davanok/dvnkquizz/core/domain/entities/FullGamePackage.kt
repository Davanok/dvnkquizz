package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class FullGamePackageDto(
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

    val author: UserProfile?,
    val rounds: List<FullGameRoundDto>
)


data class FullGamePackage(
    val id: Uuid,
    val createdAt: Instant? = null,
    val title: String,
    val description: String,
    val authorId: Uuid?,
    val difficulty: Int,
    val isPublic: Boolean,

    val author: UserProfile?,
    val rounds: List<FullGameRound>
) {
    companion object {
        val Empty = FullGamePackage(
            id = Uuid.random(),
            createdAt = null,
            title = "",
            description = "",
            authorId = null,
            difficulty = 0,
            isPublic = false,
            author = null,
            rounds = emptyList()
        )
    }
}
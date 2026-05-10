package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class FullGameRoundDto(
    val id: Uuid,
    @SerialName("package_id")
    val packageId: Uuid,
    val name: String,
    val ordinal: Int,

    val categories: List<FullGameCategoryDto>
)

data class FullGameRound(
    val id: Uuid,
    val name: String,
    val ordinal: Int,

    val categories: List<FullGameCategory>
)
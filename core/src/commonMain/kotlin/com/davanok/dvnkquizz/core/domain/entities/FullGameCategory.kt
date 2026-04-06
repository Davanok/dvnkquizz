package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class FullGameCategoryDto(
    val id: Uuid,
    @SerialName("round_id")
    val roundId: Uuid,
    val name: String,
    val ordinal: Int,

    val questions: List<QuestionDto>
)

data class FullGameCategory(
    val id: Uuid,
    val name: String,
    val ordinal: Int,

    val questions: List<Question>
)

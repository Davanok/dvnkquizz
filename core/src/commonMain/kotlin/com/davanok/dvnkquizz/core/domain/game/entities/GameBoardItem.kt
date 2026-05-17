package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class GameBoardRow(
    @SerialName("category_id")
    val categoryId: Uuid,
    @SerialName("category_name")
    val categoryName: String,
    val questions: List<GameBoardQuestion>
)

@Serializable
data class GameBoardQuestion(
    @SerialName("question_id")
    val questionId: Uuid,
    val price: Int,
    @SerialName("is_answered")
    val isAnswered: Boolean
)

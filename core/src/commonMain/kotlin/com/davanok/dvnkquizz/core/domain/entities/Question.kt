package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.enums.QuestionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Question(
    val id: Uuid,
    @SerialName("category_id") val categoryId: Uuid,
    @SerialName("question_text") val questionText: String,
    @SerialName("answer_text") val answerText: String,
    val price: Int,
    val type: QuestionType = QuestionType.NORMAL,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("media_kind") val mediaKind: MediaKind = MediaKind.NONE
)
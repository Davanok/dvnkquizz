package com.davanok.dvnkquizz.core.domain.entities

import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.enums.QuestionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class QuestionDto(
    val id: Uuid,
    @SerialName("category_id") val categoryId: Uuid,
    @SerialName("question_text") val questionText: String,
    @SerialName("answer_text") val answerText: String,
    val price: Int,
    val type: QuestionType = QuestionType.NORMAL,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("media_kind") val mediaKind: MediaKind = MediaKind.NONE
) {
    fun toDomain(): Question {
        check(mediaKind == MediaKind.NONE)

        return Question(
            id = id,
            categoryId = categoryId,
            questionText = questionText,
            answerText = answerText,
            price = price,
            type = type,
            media = null
        )
    }
    fun toDomain(mediaUrl: String, progress: Float): Question {
        check(mediaKind != MediaKind.NONE)

        return Question(
            id = id,
            categoryId = categoryId,
            questionText = questionText,
            answerText = answerText,
            price = price,
            type = type,
            media = QuestionMedia(
                url = mediaUrl,
                kind = mediaKind,
                progress = progress
            )
        )
    }
}

data class Question(
    val id: Uuid,
    val categoryId: Uuid,
    val questionText: String,
    val answerText: String,
    val price: Int,
    val type: QuestionType,
    val media: QuestionMedia?
)

data class QuestionMedia(
    val url: String,
    val kind: MediaKind,
    val progress: Float
)
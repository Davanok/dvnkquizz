package com.davanok.dvnkquizz.core.domain.game.entities

import com.davanok.dvnkquizz.core.domain.gamePackage.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.gamePackage.enums.QuestionType
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
    val type: QuestionType,
    @SerialName("media_url") val mediaUrl: String?,
    @SerialName("media_kind") val mediaKind: MediaKind
) {
    fun toDomain(): Question {
        check(mediaKind == MediaKind.NONE)

        return Question(
            id = id,
            questionText = questionText,
            answerText = answerText,
            price = price,
            type = type,
            media = null
        )
    }
    fun toDomain(mediaUrl: String, progress: Float): Question {
        check(mediaKind != MediaKind.NONE)
        checkNotNull(this.mediaUrl)

        return Question(
            id = id,
            questionText = questionText,
            answerText = answerText,
            price = price,
            type = type,
            media = QuestionMedia(
                filename = this.mediaUrl,
                url = mediaUrl,
                kind = mediaKind,
                progress = progress
            )
        )
    }
}

data class Question(
    val id: Uuid = Uuid.random(),
    val questionText: String = "",
    val answerText: String = "",
    val price: Int = 0,
    val type: QuestionType = QuestionType.NORMAL,
    val media: QuestionMedia? = null
)

data class QuestionMedia(
    val filename: String,
    val url: String,
    val kind: MediaKind,
    val progress: Float
)
package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import kotlin.uuid.Uuid

internal fun Question.toQuestionDto(categoryId: Uuid) = QuestionDto(
    id = id,
    categoryId = categoryId,
    questionText = questionText,
    answerText = answerText,
    price = price,
    type = type,
    mediaUrl = media?.url,
    mediaKind = media?.kind ?: MediaKind.NONE
)
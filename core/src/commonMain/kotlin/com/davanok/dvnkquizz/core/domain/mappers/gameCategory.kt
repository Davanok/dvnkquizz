package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategoryDto
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import kotlin.uuid.Uuid

internal inline fun FullGameCategoryDto.toFullGameCategory(
    transformQuestion: (QuestionDto) -> Question
) = FullGameCategory(
    id = id,
    name = name,
    ordinal = ordinal,
    questions = questions.map(transformQuestion)
)

internal inline fun FullGameCategory.toFullGameCategoryDto(
    roundId: Uuid,
    transformQuestion: (Question) -> QuestionDto
) = FullGameCategoryDto(
    id = id,
    roundId = roundId,
    name = name,
    ordinal = ordinal,
    questions = questions.map(transformQuestion)
)
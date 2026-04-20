package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategoryDto
import com.davanok.dvnkquizz.core.domain.entities.GameCategory
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

internal fun FullGameCategory.toFullGameCategoryDto(roundId: Uuid) = FullGameCategoryDto(
    id = id,
    roundId = roundId,
    name = name,
    ordinal = ordinal,
    questions = questions.map { it.toQuestionDto(id) }
)

fun FullGameCategory.toGameCategory() = GameCategory(
    id = id,
    name = name,
    ordinal = ordinal
)

fun GameCategory.toFullGameCategory(questions: List<Question> = emptyList()) = FullGameCategory(
    id = id,
    name = name,
    ordinal = ordinal,
    questions = questions
)
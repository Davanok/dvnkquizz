package com.davanok.dvnkquizz.core.domain.game.mappers

import com.davanok.dvnkquizz.core.domain.game.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameRoundDto
import com.davanok.dvnkquizz.core.domain.game.entities.GameRound
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.entities.QuestionDto
import kotlin.uuid.Uuid

internal inline fun FullGameRoundDto.toFullGameRound(
    transformQuestion: (QuestionDto) -> Question
) = FullGameRound(
    id = id,
    name = name,
    ordinal = ordinal,
    categories = categories.map { it.toFullGameCategory(transformQuestion) }
)

internal fun FullGameRound.toFullGameRoundDto(packageId: Uuid) = FullGameRoundDto(
    id = id,
    packageId = packageId,
    name = name,
    ordinal = ordinal,
    categories = categories.map { it.toFullGameCategoryDto(id) }
)

fun FullGameRound.toGameRound() = GameRound(
    id = id,
    name = name,
    ordinal = ordinal
)
fun GameRound.toFullGameRound(categories: List<FullGameCategory> = emptyList()) = FullGameRound(
    id = id,
    name = name,
    ordinal = ordinal,
    categories = categories
)
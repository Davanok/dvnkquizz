package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGameCategoryDto
import com.davanok.dvnkquizz.core.domain.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.entities.FullGameRoundDto
import kotlin.uuid.Uuid

internal inline fun FullGameRoundDto.toFullGameRound(
    transformCategory: (FullGameCategoryDto) -> FullGameCategory
) = FullGameRound(
    id = id,
    name = name,
    ordinal = ordinal,
    categories = categories.map(transformCategory)
)

internal inline fun FullGameRound.toFullGameRoundDto(
    packageId: Uuid,
    transformCategory: (FullGameCategory) -> FullGameCategoryDto
) = FullGameRoundDto(
    id = id,
    packageId = packageId,
    name = name,
    ordinal = ordinal,
    categories = categories.map(transformCategory)
)
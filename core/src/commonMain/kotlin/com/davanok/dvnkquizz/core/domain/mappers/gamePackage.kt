package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.entities.FullGameRoundDto

internal inline fun FullGamePackageDto.toFullGamePackage(
    transformRound: (FullGameRoundDto) -> FullGameRound
) = FullGamePackage(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    authorId = authorId,
    difficulty = difficulty,
    isPublic = isPublic,
    author = author,
    rounds = rounds.map(transformRound)
)

internal inline fun FullGamePackage.toFullGamePackageDto(
    transformRound: (FullGameRound) -> FullGameRoundDto
) = FullGamePackageDto(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    authorId = authorId,
    difficulty = difficulty,
    isPublic = isPublic,
    author = author,
    rounds = rounds.map(transformRound)
)
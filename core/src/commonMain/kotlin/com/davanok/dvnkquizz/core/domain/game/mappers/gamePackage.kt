package com.davanok.dvnkquizz.core.domain.game.mappers

import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.entities.QuestionDto

internal inline fun FullGamePackageDto.toFullGamePackage(
    transformQuestion: (QuestionDto) -> Question
) = FullGamePackage(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    authorId = authorId,
    difficulty = difficulty,
    isPublic = isPublic,
    author = author,
    rounds = rounds.map { it.toFullGameRound(transformQuestion) }
)

internal fun FullGamePackage.toFullGamePackageDto() = FullGamePackageDto(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    authorId = authorId,
    difficulty = difficulty,
    isPublic = isPublic,
    author = author,
    rounds = rounds.map { it.toFullGameRoundDto(id) }
)

internal fun FullGamePackageDto.toGamePackage() = GamePackage(
    id = id,
    createdAt = createdAt,
    title = title,
    description = description,
    authorId = authorId,
    difficulty = difficulty,
    isPublic = isPublic,
    author = author
)
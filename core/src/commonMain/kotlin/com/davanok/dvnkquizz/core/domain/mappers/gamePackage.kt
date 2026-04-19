package com.davanok.dvnkquizz.core.domain.mappers

import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto

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
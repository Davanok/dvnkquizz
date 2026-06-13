package com.davanok.dvnkquizz.ui.screens.editGamePackage

import com.davanok.dvnkquizz.core.domain.game.entities.GameCategory
import com.davanok.dvnkquizz.core.domain.game.entities.GameRound
import com.davanok.dvnkquizz.core.domain.game.entities.Question
import com.davanok.dvnkquizz.core.domain.game.mappers.toFullGameCategory
import com.davanok.dvnkquizz.core.domain.game.mappers.toFullGameRound
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage

object FullGamePackageUtils {
    fun findQuestion(gamePackage: FullGamePackage, question: Question): Question? {
        val category = gamePackage.rounds.firstNotNullOfOrNull { round ->
            round.categories.firstOrNull { it.id == question.id }
        } ?: return null

        return category.questions.firstOrNull { it.id == question.id }
    }

    fun sortGamePackage(gamePackage: FullGamePackage): FullGamePackage {
        val rounds = gamePackage.rounds.sortedBy { it.ordinal }.map { round ->
            val categories = round.categories.sortedBy { it.ordinal }.map { category ->
                val questions = category.questions.sortedBy { it.price }
                category.copy(questions = questions)
            }
            round.copy(categories = categories)
        }
        return gamePackage.copy(rounds = rounds)
    }

    fun upsertQuestion(gamePackage: FullGamePackage, question: Question): FullGamePackage {
        gamePackage.rounds.forEachIndexed { rIndex, round ->
            round.categories.forEachIndexed categories@ { cIndex, category ->
                if (category.id != question.categoryId) return@categories

                val updatedQuestions = category.questions.toMutableList()

                val qIndex = updatedQuestions.indexOfFirst { it.id == question.id }

                if (qIndex < 0) updatedQuestions.add(question)
                else updatedQuestions[qIndex] = question

                updatedQuestions.sortBy { it.price }
                val updatedCategory = category.copy(questions = updatedQuestions)

                val updatedCategories = round.categories.toMutableList()
                updatedCategories[cIndex] = updatedCategory
                val updatedRound = round.copy(categories = updatedCategories)

                val updatedRounds = gamePackage.rounds.toMutableList()
                updatedRounds[rIndex] = updatedRound

                return gamePackage.copy(rounds = updatedRounds)
            }
        }

        error("Category with id ${question.categoryId} not found")
    }

    fun upsertCategory(gamePackage: FullGamePackage, category: GameCategory): FullGamePackage {
        gamePackage.rounds.forEachIndexed rounds@ { rIndex, round ->
            if (round.id != category.roundId) return@rounds

            val updatedCategories = round.categories.toMutableList()

            val cIndex = updatedCategories.indexOfFirst { it.id == category.id }

            if (cIndex < 0) updatedCategories.add(category.toFullGameCategory())
            else updatedCategories[cIndex] = updatedCategories[cIndex].copy(
                name = category.name,
                ordinal = category.ordinal
            )

            updatedCategories.sortBy { it.ordinal }
            val updatedRound = round.copy(categories = updatedCategories)

            val updatedRounds = gamePackage.rounds.toMutableList()
            updatedRounds[rIndex] = updatedRound

            return gamePackage.copy(rounds = updatedRounds)
        }

        error("Round with id ${category.roundId} not found")
    }

    fun upsertRound(gamePackage: FullGamePackage, round: GameRound): FullGamePackage {
        val updatedRounds = gamePackage.rounds.toMutableList()

        val rIndex = updatedRounds.indexOfFirst { it.id == round.id }

        if (rIndex < 0) updatedRounds.add(round.toFullGameRound())
        else updatedRounds[rIndex] = updatedRounds[rIndex].copy(
            name = round.name,
            ordinal = round.ordinal
        )

        updatedRounds.sortBy { it.ordinal }

        return gamePackage.copy(rounds = updatedRounds)
    }
}
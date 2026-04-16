package com.davanok.dvnkquizz.ui.screens.editGamePackage

import com.davanok.dvnkquizz.core.domain.entities.FullGameCategory
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGameRound
import com.davanok.dvnkquizz.core.domain.entities.Question
import kotlin.uuid.Uuid

object FullGamePackageUtils {
    inline fun updateQuestion(gamePackage: FullGamePackage, questionId: Uuid, transform: (Question) -> Question): FullGamePackage {
        val rounds = gamePackage.rounds

        rounds.forEachIndexed { rIndex, round ->
            round.categories.forEachIndexed { cIndex, category ->
                category.questions.forEachIndexed questions@ { qIndex, question ->
                    if (question.id != questionId) return@questions

                    val updatedQuestion = transform(question)

                    val updatedQuestions = category.questions.toMutableList()
                    updatedQuestions[qIndex] = updatedQuestion
                    val updatedCategory = category.copy(questions = updatedQuestions)

                    val updatedCategories = round.categories.toMutableList()
                    updatedCategories[cIndex] = updatedCategory
                    val updatedRound = round.copy(categories = updatedCategories)

                    val updatedRounds = rounds.toMutableList()
                    updatedRounds[rIndex] = updatedRound

                    return gamePackage.copy(rounds = updatedRounds)
                }
            }
        }

        error("Question with id $questionId not found")
    }

    inline fun updateCategory(gamePackage: FullGamePackage, categoryId: Uuid, transform: (FullGameCategory) -> FullGameCategory): FullGamePackage {
        val rounds = gamePackage.rounds

        rounds.forEachIndexed { rIndex, round ->
            round.categories.forEachIndexed categories@ { cIndex, category ->
                if (category.id != categoryId) return@categories

                val updatedCategory = transform(category)

                val updatedCategories = round.categories.toMutableList()
                updatedCategories[cIndex] = updatedCategory
                val updatedRound = round.copy(categories = updatedCategories)

                val updatedRounds = rounds.toMutableList()
                updatedRounds[rIndex] = updatedRound

                return gamePackage.copy(rounds = updatedRounds)
            }
        }

        error("Category with id $categoryId not found")
    }

    inline fun updateRound(gamePackage: FullGamePackage, roundId: Uuid, transform: (FullGameRound) -> FullGameRound): FullGamePackage {
        val rounds = gamePackage.rounds.toMutableList()

        val index = rounds.indexOfFirst { it.id == roundId }
        if (index < 0)
            error("Round with id $roundId not found")

        val updatedRound = transform(rounds[index])
        rounds[index] = updatedRound

        return gamePackage.copy(rounds = rounds)
    }
}
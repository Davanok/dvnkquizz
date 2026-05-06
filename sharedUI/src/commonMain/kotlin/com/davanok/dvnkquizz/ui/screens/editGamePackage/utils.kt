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
                    updatedQuestions.sortBy { it.price }
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
                updatedCategories.sortBy { it.ordinal }
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
        check(index >= 0) { "Round with id $roundId not found" }

        val updatedRound = transform(rounds[index])
        rounds[index] = updatedRound
        rounds.sortBy { it.ordinal }

        return gamePackage.copy(rounds = rounds)
    }

    fun addQuestion(gamePackage: FullGamePackage, categoryId: Uuid, question: Question): FullGamePackage {
        val rounds = gamePackage.rounds

        rounds.forEachIndexed { rIndex, round ->
            round.categories.forEachIndexed categories@ { cIndex, category ->
                if (category.id != categoryId) return@categories

                val updatedQuestions = (category.questions + question).sortedBy { it.price }

                val updatedCategory = category.copy(questions = updatedQuestions)

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

    fun addCategory(gamePackage: FullGamePackage, roundId: Uuid, category: FullGameCategory): FullGamePackage {
        val rounds = gamePackage.rounds

        val roundIndex = rounds.indexOfFirst { it.id == roundId }
        check(roundIndex >= 0) { "Round with id $roundId not found" }

        val round = rounds[roundIndex]
        val updatedCategories = (round.categories + category).sortedBy { it.ordinal }

        val updatedRound = round.copy(categories = updatedCategories)
        val updatedRounds = rounds.toMutableList()
        updatedRounds[roundIndex] = updatedRound

        return gamePackage.copy(rounds = updatedRounds)
    }

    fun addRound(gamePackage: FullGamePackage, round: FullGameRound): FullGamePackage {
        val updatedRounds = (gamePackage.rounds + round).sortedBy { it.ordinal }
        return gamePackage.copy(rounds = updatedRounds)
    }
}
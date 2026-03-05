package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.Category
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.Round
import kotlin.uuid.Uuid

interface QuizContentRepository {
    suspend fun getPublicPackages(): List<GamePackage>
    suspend fun getMyPackages(userId: Uuid): List<GamePackage>
    suspend fun getRoundsForPackage(packageId: Uuid): List<Round>
    suspend fun getCategoriesForRound(roundId: Uuid): List<Category>
    suspend fun getQuestionsForCategory(categoryId: Uuid): List<Question>
}
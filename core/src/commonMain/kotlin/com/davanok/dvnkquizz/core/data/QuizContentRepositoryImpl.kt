package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.entities.Category
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.Round
import com.davanok.dvnkquizz.core.domain.repositories.QuizContentRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class QuizContentRepositoryImpl(
    private val postgrest: Postgrest
) : QuizContentRepository {

    // --- Packages ---

    override suspend fun getPublicPackages(): List<GamePackage> {
        return postgrest["game_packages"]
            .select {
                filter { eq("is_public", true) }
            }.decodeList()
    }

    override suspend fun getMyPackages(userId: Uuid): List<GamePackage> {
        return postgrest["game_packages"]
            .select {
                filter { eq("author_id", userId) }
            }.decodeList()
    }

    // --- Rounds, Categories, and Questions ---

    override suspend fun getRoundsForPackage(packageId: Uuid): List<Round> {
        return postgrest["rounds"]
            .select {
                filter { eq("package_id", packageId) }
            }.decodeList()
    }

    override suspend fun getCategoriesForRound(roundId: Uuid): List<Category> {
        return postgrest["categories"]
            .select {
                filter { eq("round_id", roundId) }
            }.decodeList()
    }

    override suspend fun getQuestionsForCategory(categoryId: Uuid): List<Question> {
        return postgrest["questions"]
            .select {
                filter { eq("category_id", categoryId) }
            }.decodeList()
    }
}
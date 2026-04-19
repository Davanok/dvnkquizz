package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.QuestionMedia
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface UserGamePackagesRepository {
    suspend fun getUserGamePackages(): Result<List<GamePackage>>
    suspend fun getGamePackage(packageId: Uuid): Result<FullGamePackage>

    fun uploadQuestionMedia(packageId: Uuid, bytes: ByteArray, mimeType: String): Flow<Result<QuestionMedia>>
    suspend fun deleteQuestionMedia(questionId: Uuid): Result<Unit>

    suspend fun updateGamePackage(gamePackage: FullGamePackage): Result<Unit>

    suspend fun updatePackageDraft(draft: FullGamePackage): Result<Unit>
    suspend fun getPackageDraft(draftId: Uuid): Result<FullGamePackage?>
    suspend fun getAllPackageDrafts(): Result<List<GamePackage>>
}
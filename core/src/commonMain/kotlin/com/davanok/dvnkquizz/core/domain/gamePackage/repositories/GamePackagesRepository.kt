package com.davanok.dvnkquizz.core.domain.gamePackage.repositories

import androidx.paging.PagingData
import com.davanok.dvnkquizz.core.domain.game.entities.QuestionMedia
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.GamePackage
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface GamePackagesRepository {
    fun getPagedPackages(query: String): Flow<PagingData<GamePackage>>

    suspend fun getUserGamePackages(): Result<List<GamePackage>>
    suspend fun getGamePackage(packageId: Uuid): Result<FullGamePackage>

    fun uploadQuestionMedia(packageId: Uuid, bytes: ByteArray, mimeType: String): Flow<Result<QuestionMedia>>
    suspend fun deleteQuestionMedia(questionId: Uuid): Result<Unit>

    suspend fun updateGamePackage(gamePackage: FullGamePackage): Result<Unit>

    suspend fun updatePackageDraft(draft: FullGamePackage): Result<Unit>
    suspend fun getPackageDraft(draftId: Uuid): Result<FullGamePackage?>
    suspend fun getAllPackageDrafts(): Result<List<GamePackage>>
}
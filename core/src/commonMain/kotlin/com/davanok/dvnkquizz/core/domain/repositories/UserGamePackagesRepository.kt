package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import kotlin.uuid.Uuid

interface UserGamePackagesRepository {
    suspend fun getUserGamePackages(): Result<List<GamePackage>>
    suspend fun getGamePackage(packageId: Uuid): Result<FullGamePackage>

    suspend fun updateGamePackage(gamePackage: FullGamePackage): Result<Unit>
}
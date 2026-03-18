package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface UserProfileRepository {
    fun observeProfile(): Flow<Result<UserProfile>>

    suspend fun updateProfile(nickname: String, image: Uuid?): Result<Unit>
}
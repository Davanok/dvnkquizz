package com.davanok.dvnkquizz.core.domain.repositories

import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<Result<UserProfile>>

    suspend fun setNickname(nickname: String): Result<Unit>
    suspend fun setImage(image: ByteArray?): Result<Unit>
}
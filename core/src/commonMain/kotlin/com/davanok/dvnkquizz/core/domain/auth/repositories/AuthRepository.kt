package com.davanok.dvnkquizz.core.domain.auth.repositories

import com.davanok.dvnkquizz.core.domain.auth.entities.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeUser(): Flow<Result<User?>>

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun resendEmail(email: String): Result<Unit>
    suspend fun logOut(): Result<Unit>
}
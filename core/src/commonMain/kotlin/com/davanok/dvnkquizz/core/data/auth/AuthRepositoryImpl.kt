package com.davanok.dvnkquizz.core.data.auth

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.core.id.toUuid
import com.davanok.dvnkquizz.core.domain.auth.entities.User
import com.davanok.dvnkquizz.core.domain.auth.repositories.AuthRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@Inject
@ContributesBinding(AppScope::class)
class AuthRepositoryImpl(
    private val auth: Auth,
    logger: Logger
) : AuthRepository {
    private val logger = logger.withTag(TAG)

    override fun observeUser(): Flow<Result<User?>> =
        auth.sessionStatus
            .onEach { status ->
                logger.d { "Auth status changed: $status" }
            }
            .mapNotNull { status ->
                when (status) {
                    is SessionStatus.Authenticated -> Result.success(status.session.user?.toUser())
                    is SessionStatus.NotAuthenticated -> Result.success(null)
                    is SessionStatus.RefreshFailure -> Result.failure(Exception("Session refresh failure"))
                    SessionStatus.Initializing -> null // Skip emission during startup
                }
            }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        runCatching<Unit> {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }.onSuccess {
            signInWithEmail(email, password)
        }

    override suspend fun resendEmail(email: String): Result<Unit> =
        runCatching {
            auth.resendEmail(type = OtpType.Email.EMAIL, email = email)
        }

    override suspend fun logOut(): Result<Unit> =
        runCatching {
            auth.signOut()
        }

    private fun UserInfo.toUser() = User(
        id = id.toUuid(),
        email = email
    )

    companion object {
        private const val TAG = "AuthRepository"
    }
}
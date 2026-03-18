package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.repositories.UserProfileRepository
import com.davanok.dvnkquizz.core.utils.currentUserId
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class UserProfileRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val logger: Logger
): UserProfileRepository {
    @OptIn(SupabaseExperimental::class)
    override fun observeProfile(): Flow<Result<UserProfile>> =
        postgrest.from("users")
            .selectSingleValueAsFlow(UserProfile::id) {
                UserProfile::id eq auth.currentUserId
            }.toResultFLow()

    override suspend fun updateProfile(nickname: String, image: Uuid?): Result<Unit> = runCatching {
        postgrest.from("users")
            .update({
                UserProfile::nickname setTo nickname
                UserProfile::image setTo image
            }) {
                filter {
                    UserProfile::id eq auth.currentUserId
                }
            }
    }
}
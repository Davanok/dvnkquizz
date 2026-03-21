package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.entities.UserProfileDto
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
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class UserProfileRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val filesSource: FilesSource,
    private val logger: Logger
): UserProfileRepository {
    @OptIn(SupabaseExperimental::class)
    override fun observeProfile(): Flow<Result<UserProfile>> =
        postgrest.from("users")
            .selectSingleValueAsFlow(UserProfileDto::id) {
                UserProfileDto::id eq auth.currentUserId
            }.toResultFLow().map { result ->
                result.mapCatching { profileDto ->
                    val image = profileDto.image?.let { image ->
                        filesSource.fetchSource("profiles", image).getOrNull()
                    }

                    profileDto.toDomain(image = image)
                }
            }

    override suspend fun updateProfile(nickname: String, image: Uuid?): Result<Unit> = runCatching {
        postgrest.from("users")
            .update({
                UserProfileDto::nickname setTo nickname
            }) {
                filter {
                    UserProfileDto::id eq auth.currentUserId
                }
            }
    }
}
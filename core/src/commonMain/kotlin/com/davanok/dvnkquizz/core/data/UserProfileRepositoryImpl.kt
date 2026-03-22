package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FileDownloadStatus
import com.davanok.dvnkquizz.core.domain.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.entities.UserProfileDto
import com.davanok.dvnkquizz.core.domain.repositories.UserProfileRepository
import com.davanok.dvnkquizz.core.utils.currentUserId
import com.davanok.dvnkquizz.core.utils.toFileDownloadStatus
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.downloadAuthenticatedAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(AppScope::class)
class UserProfileRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val logger: Logger
): UserProfileRepository {
    private fun getUserImageFlow(path: String): Flow<FileDownloadStatus> =
        storage.from("profiles").downloadAuthenticatedAsFlow(path)
            .map { it.toFileDownloadStatus() }
            .catch {
                logger.e(it) { "failed to get user image flow" }
                emit(FileDownloadStatus.Error(it))
            }

    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<Result<UserProfile>> {
        var cachedImagePath: String? = null
        var currentImageFlow: Flow<FileDownloadStatus>? = null

        return postgrest.from("users")
            .selectSingleValueAsFlow(UserProfileDto::id) {
                UserProfileDto::id eq auth.currentUserId
            }
            .flatMapLatest { profileDto ->
                if (profileDto.image != cachedImagePath) {
                    cachedImagePath = profileDto.image
                    currentImageFlow = profileDto.image?.let {
                        getUserImageFlow(profileDto.image)
                    }
                }

                currentImageFlow?.map { profileDto.toDomain(it) } ?: flowOf(profileDto.toDomain(null))
            }
            .toResultFLow()
    }

    override suspend fun setNickname(nickname: String): Result<Unit> = runCatching {
        postgrest.from("users")
            .update({ UserProfileDto::nickname setTo nickname }) {
                filter { UserProfileDto::id eq auth.currentUserId }
            }
    }

    override suspend fun setImage(image: ByteArray?): Result<Unit> = runCatching {
        TODO("Not yet implemented")
    }
}
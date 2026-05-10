package com.davanok.dvnkquizz.core.data.user

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.core.id.currentUserId
import com.davanok.dvnkquizz.core.core.result.toResultFLow
import com.davanok.dvnkquizz.core.domain.auth.entities.UserProfile
import com.davanok.dvnkquizz.core.domain.auth.repositories.UserProfileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.io.files.Path
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class UserProfileRepositoryImpl(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val storage: Storage,
    logger: Logger
) : UserProfileRepository {
    private val logger = logger.withTag(TAG)

    @OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<Result<UserProfile>> =
        postgrest.from("users")
            .selectSingleValueAsFlow(UserProfile::id) {
                UserProfile::id eq auth.currentUserId
            }
            .mapLatest { profile ->
                val profileImageUrl = profile.image?.let {
                    storage.from("profiles").createSignedUrl(it, 1.minutes)
                }

                profile.copy(image = profileImageUrl)
            }
            .toResultFLow()

    override suspend fun setNickname(nickname: String): Result<Unit> = runCatching<Unit> {
        postgrest.from("users")
            .update({ UserProfile::nickname setTo nickname }) {
                filter { UserProfile::id eq auth.currentUserId }
            }
    }.onFailure {
        logger.e(it) { "failed to set profile nickname" }
    }

    override suspend fun setImage(image: ByteArray?): Result<Unit> = runCatching<Unit> {
        val currentUser = checkNotNull(auth.currentUserOrNull())
        val filename =
            image?.let { Path(currentUser.id, Uuid.random().toString() + ".image").toString() }

        if (image != null && filename != null)
            storage.from("profiles")
                .upload(filename, image)

        postgrest.from("users")
            .update({
                UserProfile::image setTo filename
            }) {
                filter { UserProfile::id eq currentUser.id }
            }
    }.onFailure {
        logger.e(it) { "failed to set profile image" }
    }

    companion object {
        private const val TAG = "ObserveSessionRepository"
    }
}
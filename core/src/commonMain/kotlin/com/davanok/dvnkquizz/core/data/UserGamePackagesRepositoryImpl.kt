package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameCategory
import com.davanok.dvnkquizz.core.domain.mappers.toFullGamePackage
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameRound
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import com.davanok.dvnkquizz.core.utils.currentUserId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
class UserGamePackagesRepositoryImpl(
    private val postgrest: Postgrest,
    private val storage: Storage,
    private val auth: Auth,
    logger: Logger
): UserGamePackagesRepository {
    private val logger = logger.withTag(TAG)

    private suspend fun QuestionDto.toQuestion(): Question {
        if (mediaKind == MediaKind.NONE || mediaUrl == null)
            return toDomain()
        val mediaUrl = storage
            .from("questions")
            .createSignedUrl(mediaUrl, MEDIA_URL_EXPIRE_DURATION)
        return toDomain(mediaUrl, 1f)
    }

    override suspend fun getUserGamePackages(): Result<List<FullGamePackage>> {
        logger.d { "get user game packages list" }

        return runCatching {
            val userId = checkNotNull(auth.currentUserId) { "User not authorized" }

            postgrest.from("game_packages")
                .select(Columns.raw(GAME_PACKAGE_QUERY)) {
                    filter {
                        FullGamePackageDto::authorId eq userId
                    }
                }.decodeList<FullGamePackageDto>()
                .map { pkg ->
                    pkg.toFullGamePackage { round ->
                        round.toFullGameRound { category ->
                            category.toFullGameCategory { question ->
                                question.toQuestion()
                            }
                        }
                    }
                }
        }.onFailure {
            logger.e(it) { "failed to get user packages" }
        }
    }

    override suspend fun getGamePackage(packageId: Uuid): Result<FullGamePackage> {
        logger.d { "get user game package: packageId=$packageId" }

        return runCatching {
            val userId = checkNotNull(auth.currentUserId) { "User not authorized" }

            postgrest.from("game_packages")
                .select(Columns.raw(GAME_PACKAGE_QUERY)) {
                    filter {
                        and {
                            FullGamePackageDto::id eq packageId
                            FullGamePackageDto::authorId eq userId
                        }
                    }
                }.decodeSingle<FullGamePackageDto>()
                .let { pkg ->
                    pkg.toFullGamePackage { round ->
                        round.toFullGameRound { category ->
                            category.toFullGameCategory { question ->
                                question.toQuestion()
                            }
                        }
                    }
                }
        }.onFailure {
            logger.e(it) { "failed to get user package" }
        }
    }

    override suspend fun updateGamePackage(gamePackage: FullGamePackage): Result<Unit> {
        logger.d { "update user game package: packageId=${gamePackage.id}" }

        return runCatching<Unit> {
            val userId = checkNotNull(auth.currentUserId) { "User not authorized" }

            postgrest.from("game_packages")
                .update(gamePackage) {
                    filter {
                        FullGamePackageDto::id eq gamePackage.id
                        FullGamePackageDto::authorId eq userId
                    }
                }
        }.onFailure {
            logger.e(it) { "failed to get user packages" }
        }
    }


    companion object {
        private const val TAG = "EditGamePackageRepository"
        private val GAME_PACKAGE_QUERY = """
            *, 
            author:users(*)
            rounds:rounds(
                *,
                categories:categories(
                   *,
                   questions:questions(*)
                )
            )
            """.trimIndent()
        private val MEDIA_URL_EXPIRE_DURATION = 1.hours
    }
}
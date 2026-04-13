package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.GamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.QuestionDto
import com.davanok.dvnkquizz.core.domain.entities.QuestionMedia
import com.davanok.dvnkquizz.core.domain.enums.MediaKind
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameCategory
import com.davanok.dvnkquizz.core.domain.mappers.toFullGamePackage
import com.davanok.dvnkquizz.core.domain.mappers.toFullGameRound
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import com.davanok.dvnkquizz.core.utils.currentUserId
import com.davanok.dvnkquizz.core.utils.mediaKindForMimeType
import com.davanok.dvnkquizz.core.utils.mimeTypeToFileExtension
import com.davanok.dvnkquizz.core.utils.toResultFLow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    override suspend fun getUserGamePackages(): Result<List<GamePackage>> {
        logger.d { "get user game packages list" }

        return runCatching {
            val userId = checkNotNull(auth.currentUserId) { "User not authorized" }

            postgrest.from("game_packages")
                .select(Columns.raw(GAME_PACKAGE_QUERY)) {
                    filter {
                        FullGamePackageDto::authorId eq userId
                    }
                }.decodeList<GamePackage>()
        }.onFailure {
            logger.e(it) { "failed to get user packages" }
        }
    }

    override suspend fun getGamePackage(packageId: Uuid): Result<FullGamePackage> {
        logger.d { "get user game package: packageId=$packageId" }

        return runCatching {
            val userId = checkNotNull(auth.currentUserId) { "User not authorized" }

            postgrest.from("game_packages")
                .select(Columns.raw(FULL_GAME_PACKAGE_QUERY)) {
                    filter {
                        and {
                            FullGamePackageDto::id eq packageId
                            FullGamePackageDto::authorId eq userId
                        }
                    }
                }.decodeSingleOrNull<FullGamePackageDto>()
                .let { pkg ->
                    pkg?.toFullGamePackage { round ->
                        round.toFullGameRound { category ->
                            category.toFullGameCategory { question ->
                                question.toQuestion()
                            }
                        }
                    } ?: FullGamePackage.Empty
                }
        }.onFailure {
            logger.e(it) { "failed to get user package" }
        }
    }

    override fun uploadQuestionMedia(
        packageId: Uuid,
        bytes: ByteArray,
        mimeType: String
    ): Flow<Result<QuestionMedia>> = flow {
        val currentUserId = checkNotNull(auth.currentUserId) { "Unauthorized user cannot upload question media" }

        val filename = Uuid.random().toString() + '.' + mimeTypeToFileExtension(mimeType)
        val path = "$packageId/$filename"

        val mediaKind = mediaKindForMimeType(mimeType)

        runCatching {
            val packageExists = postgrest
                .from("game_packages")
                .select(Columns.list("id")) {
                    single()
                }.decodeSingleOrNull<Uuid>() != null

            if (!packageExists) {
                val gamePackage = GamePackageDto(
                    id = packageId,
                    title = "[Draft]",
                    description = "",
                    authorId = currentUserId
                )
                postgrest.from("game_packages")
                    .insert(gamePackage)
            }
        }.onFailure { thr ->
            emit(Result.failure(thr))
            return@flow
        }

        storage.from("questions")
            .uploadAsFlow(path, bytes)
            .map { status ->
                when (status) {
                    is UploadStatus.Progress -> QuestionMedia(
                        url = path,
                        kind = mediaKind,
                        progress = status.totalBytesSend.toFloat() / status.contentLength
                    )
                    is UploadStatus.Success -> QuestionMedia(
                        url = path,
                        kind = mediaKind,
                        progress = 1f
                    )
                }
            }.toResultFLow().collect(this)
    }

    override suspend fun updateGamePackage(gamePackage: FullGamePackage): Result<Unit> {
        logger.d { "update user game package: packageId=${gamePackage.id}" }

        return runCatching<Unit> {
            postgrest.rpc(
                "upsert_full_game_package",
                gamePackage
            )
        }.onFailure {
            logger.e(it) { "failed to get user packages" }
        }
    }


    companion object {
        private const val TAG = "EditGamePackageRepository"

        private const val GAME_PACKAGE_QUERY = "*, author:users(*)"
        private val FULL_GAME_PACKAGE_QUERY = """
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
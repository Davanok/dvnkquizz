package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.ExternalFile
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.authenticatedRequest

@Inject
class FilesSource(
    private val storage: Storage,
    private val logger: Logger
) {
    suspend fun fetchSource(bucket: String, url: String): Result<ExternalFile> =
        runCatching {
            val (token, url) = storage.from(bucket)
                .authenticatedRequest(url)

            ExternalFile(
                url = url,
                accessToken = token
            )
        }.onFailure {
            logger.e(it) { "failed to fetch source" }
        }
}
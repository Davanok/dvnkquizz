package com.davanok.dvnkquizz.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.repositories.GamePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
class GamePackageRepositoryImpl(
    private val postgrest: Postgrest,
    logger: Logger
): GamePackageRepository {
    private val logger = logger.withTag(TAG)
    private suspend fun getPage(query: String, from: Long, count: Int): List<GamePackage> {
        logger.d { "getPage query=$query from=$from count=$count" }

        return postgrest.from("game_packages").select {
            if (query.isNotBlank()) filter {
                val queryPattern = "%$query%"
                or {
                    GamePackage::title ilike queryPattern
                    GamePackage::description ilike queryPattern
                }
            }
            range(from, from + count)
        }.decodeList()
    }

    override fun getPagedPackages(query: String): Flow<PagingData<GamePackage>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { PackagePagingSource(::getPage, query) }
        ).flow
    }

    companion object {
        private const val TAG = "GamePackageRepository"
        private const val PAGE_SIZE = 20
    }
}
package com.davanok.dvnkquizz.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.repositories.GamePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
class GamePackageRepositoryImpl(private val postgrest: Postgrest): GamePackageRepository {
    override fun getPagedPackages(query: String): Flow<PagingData<GamePackage>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { PackagePagingSource(postgrest, query) }
        ).flow
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
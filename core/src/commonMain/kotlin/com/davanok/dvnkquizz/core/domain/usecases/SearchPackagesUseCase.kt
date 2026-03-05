package com.davanok.dvnkquizz.core.domain.usecases

import androidx.paging.PagingData
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.repositories.GamePackageRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class SearchPackagesUseCase(private val repository: GamePackageRepository) {
    fun getPagedPackages(query: String): Flow<PagingData<GamePackage>> =
        repository.getPagedPackages(query)
}
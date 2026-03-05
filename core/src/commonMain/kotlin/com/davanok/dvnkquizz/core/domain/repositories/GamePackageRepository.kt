package com.davanok.dvnkquizz.core.domain.repositories

import androidx.paging.PagingData
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import kotlinx.coroutines.flow.Flow

interface GamePackageRepository {
    fun getPagedPackages(query: String): Flow<PagingData<GamePackage>>
}
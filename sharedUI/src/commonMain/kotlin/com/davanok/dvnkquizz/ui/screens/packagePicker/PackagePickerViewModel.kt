package com.davanok.dvnkquizz.ui.screens.packagePicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.repositories.GamePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

@Inject
@ViewModelKey(PackagePickerViewModel::class)
@ContributesIntoMap(AppScope::class)
class PackagePickerViewModel(
    private val packageRepository: GamePackageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val packages: Flow<PagingData<GamePackage>> = _searchQuery
        .debounce(300L) // Wait for user to stop typing
        .flatMapLatest { query ->
            packageRepository.getPagedPackages(query)
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
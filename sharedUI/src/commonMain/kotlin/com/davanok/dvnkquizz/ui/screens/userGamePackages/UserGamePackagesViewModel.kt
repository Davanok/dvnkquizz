package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Inject
@ViewModelKey(UserGamePackagesViewModel::class)
@ContributesIntoMap(AppScope::class)
class UserGamePackagesViewModel(
    private val repository: UserGamePackagesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserGamePackagesScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val draftsDeferred = async { repository.getAllPackageDrafts() }
            val externalDeferred = async { repository.getUserGamePackages() }

            val draftsResult = draftsDeferred.await()
            val externalResult = externalDeferred.await()

            _uiState.update { state ->
                val externalValue = externalResult.getOrElse { emptyList() }
                val externalException = externalResult.exceptionOrNull()

                val draftValue = draftsResult.getOrElse { emptyList() }
                val draftException = draftsResult.exceptionOrNull()

                state.copy(
                    isExternalLoading = false,
                    isDraftsLoading = false,
                    externalError = externalException?.message,
                    draftsError = draftException?.message,
                    external = externalValue,
                    drafts = draftValue
                )
            }
        }
    }
}
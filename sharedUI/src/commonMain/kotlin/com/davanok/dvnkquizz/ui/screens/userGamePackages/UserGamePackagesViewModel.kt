package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Inject
@ViewModelKey(UserGamePackagesViewModel::class)
@ContributesIntoMap(AppScope::class)
class UserGamePackagesViewModel(
    private val repository: UserGamePackagesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UserGamePackagesScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadGamePackages()
        }
    }

    private suspend fun loadGamePackages() {
        _uiState.update { it.copy(isLoading = true) }

        repository.getUserGamePackages().let { result ->
            _uiState.update { state ->
                result.fold(
                    onSuccess = { packages ->
                        state.copy(
                            isLoading = false,
                            gamePackages = packages
                        )
                    },
                    onFailure = { thr ->
                        state.copy(
                            isLoading = false,
                            errorMessage = thr.message
                        )
                    }
                )
            }
        }
    }
}
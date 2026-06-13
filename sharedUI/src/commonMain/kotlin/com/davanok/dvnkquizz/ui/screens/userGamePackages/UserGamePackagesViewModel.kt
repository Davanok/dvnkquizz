package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.gamePackage.repositories.GamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class UserGamePackagesViewModel(
    private val repository: GamePackagesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserGamePackagesScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
        observeDrafts()
    }

    fun loadData() {
        viewModelScope.launch {
            val externalResult = repository.getUserGamePackages()

            _uiState.update { state ->
                val externalValue = externalResult.getOrElse { emptyList() }
                val externalException = externalResult.exceptionOrNull()

                state.copy(
                    isExternalLoading = false,
                    isDraftsLoading = false,
                    externalError = externalException?.message,
                    external = externalValue,
                )
            }
        }
    }

    private fun observeDrafts() =
        repository
            .observeAllPackageDrafts()
            .onEach { result ->
                result.fold(
                    onSuccess = { drafts ->
                        _uiState.update {
                            it.copy(
                                drafts = drafts,
                                draftsError = null
                            )
                        }
                    },
                    onFailure = { thr ->
                        _uiState.update {
                            it.copy(
                                draftsError = thr.toString()
                            )
                        }
                    }
                )
            }.launchIn(viewModelScope)
}
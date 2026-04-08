package com.davanok.dvnkquizz.ui.screens.editGamePackage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.entities.FullGamePackage
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class EditGamePackageViewModel(
    @Assisted private val packageId: Uuid?,
    private val repository: UserGamePackagesRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(EditGamePackageUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadGamePackage()
        }
    }

    private suspend fun loadGamePackage() {
        if (packageId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    gamePackage = FullGamePackage.Empty
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val result = repository.getGamePackage(packageId)

        _uiState.update { state ->
            result.fold(
                onSuccess = { gamePackage ->
                    state.copy(
                        isLoading = false,
                        errorMessage = null,
                        gamePackage = gamePackage
                    )
                },
                onFailure = { thr ->
                    state.copy(
                        isLoading = false,
                        errorMessage = thr.message,
                        gamePackage = FullGamePackage.Empty
                    )
                }
            )
        }
    }

    fun eventSink(event: EditGamePackageUiEvent) {

    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted packageId: Uuid?): EditGamePackageViewModel
    }
}
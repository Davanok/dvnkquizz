package com.davanok.dvnkquizz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.auth.repositories.AuthRepository
import com.davanok.dvnkquizz.core.domain.settings.repositories.AppSettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class AppViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeUser()
        observeSettings()
    }

    private fun observeUser() = viewModelScope.launch {
        authRepository
            .observeUser()
            .collect { result ->
                _uiState.update { state ->
                    result.fold(
                        onSuccess = { user ->
                            state.copy(
                                isLoading = false,
                                user = user,
                                errorMessage = null
                            )
                        },
                        onFailure = { thr ->
                            state.copy(
                                isLoading = false,
                                user = null,
                                errorMessage = thr.message
                            )
                        }
                    )
                }
            }
    }

    fun observeSettings() = viewModelScope.launch {
        appSettingsRepository.observeAppSettings().collect { appSettings ->
            _uiState.update {
                it.copy(
                    theme = appSettings.theme
                )
            }
        }
    }
}
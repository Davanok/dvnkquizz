package com.davanok.dvnkquizz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.auth.entities.User
import com.davanok.dvnkquizz.core.domain.auth.repositories.AuthRepository
import com.davanok.dvnkquizz.core.domain.settings.repositories.AppSettingsRepository
import com.davanok.dvnkquizz.ui.navigation.Route
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
                    result.onSuccess {
                        handleAuthNavigation(it)
                    }.fold(
                        onSuccess = { user ->
                            state.copy(
                                user = user,
                                errorMessage = null
                            )
                        },
                        onFailure = { thr ->
                            state.copy(
                                user = null,
                                errorMessage = thr.message
                            )
                        }
                    )
                }
            }
    }

    private fun handleAuthNavigation(user: User?) {
        val currentRoute = _uiState.value.backStack.lastOrNull()

        when {
            user == null -> {
                if (currentRoute != Route.Auth) {
                    navigationEventSink(NavigationEvent.Replace(Route.Auth))
                }
            }
            currentRoute == Route.Auth || currentRoute == Route.PlaceHolder -> {
                navigationEventSink(NavigationEvent.Replace(Route.Home))
            }
        }
    }

    fun navigationEventSink(event: NavigationEvent) = _uiState.update {
        val backStack = it.backStack.toMutableList()
        when (event) {
            NavigationEvent.Back -> backStack.removeLastOrNull()
            is NavigationEvent.Navigate -> backStack.add(event.route)
            is NavigationEvent.Replace -> backStack[backStack.lastIndex] = event.route
        }
        it.copy(backStack = backStack)
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
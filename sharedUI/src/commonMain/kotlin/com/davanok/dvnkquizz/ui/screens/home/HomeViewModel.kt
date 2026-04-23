package com.davanok.dvnkquizz.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.enums.AppTheme
import com.davanok.dvnkquizz.core.domain.repositories.AuthRepository
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.domain.repositories.SettingsRepository
import com.davanok.dvnkquizz.core.domain.repositories.UserProfileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val gameSessionRepository: GameSessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeProfile()
        observeAppSettings()
    }

    fun onJoinClicked(
        inviteCode: String,
        onSuccess: (Uuid) -> Unit
    ) {
        if (inviteCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSessionLoading = true) }

            gameSessionRepository.joinSession(inviteCode.uppercase())
                .onSuccess { response ->
                    onSuccess(response.sessionId)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSessionLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun onCreateGame(packageId: Uuid, onSuccess: (Uuid) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSessionLoading = true) }
            gameSessionRepository.createSession(packageId)
                .onSuccess { response ->
                    onSuccess(response.sessionId)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSessionLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    private fun observeProfile() = viewModelScope.launch {
        userProfileRepository.observeProfile().collect { result ->
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            nickname = profile.nickname,
                            imageUrl = profile.image
                        )
                    }
                },
                onFailure = { thr ->
                    _uiState.update {
                        it.copy(
                            isProfileLoading = false,
                            errorMessage = thr.message
                        )
                    }
                }
            )
        }
    }

    fun setNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname, nicknameChanged = true) }
    }

    fun setImage(image: ByteArray?) = viewModelScope.launch {
        _uiState.update { it.copy(isProfileLoading = true) }
        userProfileRepository.setImage(image = image)
            .onFailure { thr ->
                _uiState.update { it.copy(errorMessage = thr.message) }
            }
    }

    fun submitNickname() = viewModelScope.launch {
        _uiState.update { it.copy(nicknameChanged = false, isProfileLoading = true) }
        userProfileRepository.setNickname(nickname = uiState.value.nickname)
            .onFailure { thr ->
                _uiState.update { it.copy(errorMessage = thr.message) }
            }
    }

    fun logOut() = viewModelScope.launch {
        authRepository.logOut()
    }

    fun observeAppSettings() = viewModelScope.launch {
        settingsRepository.observeAppSettings().collect { settings ->
            _uiState.update {
                it.copy(
                    appSettings = settings
                )
            }
        }
    }
    fun setAppTheme(theme: AppTheme) = viewModelScope.launch {
        settingsRepository.updateAppSettings(
            uiState.value.appSettings.copy(theme = theme)
        )
    }
}
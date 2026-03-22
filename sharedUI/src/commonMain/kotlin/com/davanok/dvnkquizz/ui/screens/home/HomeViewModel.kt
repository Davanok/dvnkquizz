package com.davanok.dvnkquizz.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.repositories.AuthRepository
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.domain.repositories.UserProfileRepository
import com.davanok.dvnkquizz.ui.domain.ImageStatus
import com.davanok.dvnkquizz.ui.domain.toImageStatus
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlin.uuid.Uuid

@Inject
@ViewModelKey(HomeViewModel::class)
@ContributesIntoMap(AppScope::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val gameSessionRepository: GameSessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    fun onJoinClicked(
        inviteCode: String,
        onSuccess: (Uuid) -> Unit
    ) {
        if (inviteCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            gameSessionRepository.joinSession(inviteCode.uppercase())
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(response.sessionId)
                }
                .onFailure { e ->
                    Logger.w(e) { "Unknown Error" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    fun onCreateGame(packageId: Uuid, onSuccess: (Uuid) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            gameSessionRepository.createSession(packageId)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(response.sessionId)
                }
                .onFailure { e ->
                    Logger.w(e) { "Failed to create game" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }

    private fun observeProfile() = viewModelScope.launch {
        userProfileRepository.observeProfile().collect { result ->
            result.mapCatching { profile ->
                val imageStatus = profile.image?.toImageStatus()

                if (imageStatus is ImageStatus.Error)
                    _uiState.update { it.copy(errorMessage = imageStatus.throwable.message) }

                profile.nickname to imageStatus
            }.fold(
                onSuccess = { (nickname, image) ->
                    _uiState.update {
                        it.copy(
                            nickname = nickname,
                            image = image
                        )
                    }
                },
                onFailure = { thr ->
                    _uiState.update {
                        it.copy(
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

    fun setImage(image: Path) = viewModelScope.launch {
        userProfileRepository.setImage(image = null) // TODO
    }

    fun submitNickname() = viewModelScope.launch {
        _uiState.update { it.copy(nicknameChanged = false) }
        userProfileRepository.setNickname(nickname = uiState.value.nickname)
    }

    fun logOut() = viewModelScope.launch {
        authRepository.logOut()
    }
}
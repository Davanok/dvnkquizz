package com.davanok.dvnkquizz.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.domain.usecases.HostGameUseCase
import com.davanok.dvnkquizz.core.domain.usecases.JoinGameUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey(HomeViewModel::class)
@ContributesIntoMap(AppScope::class)
class HomeViewModel(
    private val joinGameUseCase: JoinGameUseCase,
    private val hostGameUseCase: HostGameUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onJoinClicked(
        nickname: String,
        inviteCode: String,
        onSuccess: (Uuid) -> Unit
    ) {
        if (nickname.isBlank() || inviteCode.isBlank()) return

        viewModelScope.launch {
            _uiState.value = HomeScreenUiState.Loading
            joinGameUseCase.joinGame(inviteCode.uppercase(), nickname)
                .onSuccess { sessionId ->
                    _uiState.value = HomeScreenUiState.Idle
                    onSuccess(sessionId)
                }
                .onFailure { e ->
                    Logger.w(e) { "Unknown Error" }
                    _uiState.value = HomeScreenUiState.Error(e.message ?: "Unknown Error")
                }
        }
    }

    fun onCreateGame(packageId: Uuid, nickname: String, onSuccess: (Uuid) -> Unit) {
        viewModelScope.launch {
            _uiState.value = HomeScreenUiState.Loading
            hostGameUseCase.createSession(packageId, nickname)
                .onSuccess {
                    onSuccess(it.sessionId)
                }
                .onFailure {
                    Logger.w(it) { "Failed to create game" }
                    _uiState.value = HomeScreenUiState.Error("Failed to create game")
                }
        }
    }
}
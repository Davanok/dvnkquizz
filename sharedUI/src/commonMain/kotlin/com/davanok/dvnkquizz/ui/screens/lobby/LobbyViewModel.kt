package com.davanok.dvnkquizz.ui.screens.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class LobbyViewModel(
    @Assisted private val sessionId: Uuid,
    private val repository: GameSessionRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(LobbyScreenUiState())
    val uiState: StateFlow<LobbyScreenUiState> = _uiState.asStateFlow()

    init {
        observeLobbyData()
    }

    private fun observeLobbyData() {
        viewModelScope.launch {
            repository.observeGameSessionStatus(sessionId)
                .collect { result ->
                    result.onSuccess { data ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                session = data.session,
                                participants = data.participants,
                                isHost = data.isHost
                            )
                        }
                    }.onFailure { thr ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = thr.message
                            )
                        }
                    }
                }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            repository.updateSessionStatus(sessionId, SessionStatus.IN_PROGRESS)
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "Failed to start game.") }
                }
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted sessionId: Uuid): LobbyViewModel
    }
}
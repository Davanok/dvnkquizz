package com.davanok.dvnkquizz.ui.screens.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import com.davanok.dvnkquizz.core.domain.usecases.GetLobbyDataUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class LobbyViewModel(
    @Assisted private val sessionId: Uuid,
    private val repository: GameSessionRepository,
    private val lobbyDataUseCase: GetLobbyDataUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(LobbyScreenUiState())
    val uiState: StateFlow<LobbyScreenUiState> = _uiState.asStateFlow()

    init {
        observeLobbyData()
    }

    private fun observeLobbyData() {
        viewModelScope.launch {
            lobbyDataUseCase.observeLobbyData(sessionId)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .collect { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = data.session,
                            participants = data.participants,
                            isHost = data.session.hostId == Uuid.NIL
                        )
                    }
                }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            try {
                repository.updateSessionStatus(sessionId, SessionStatus.IN_PROGRESS)
            } catch (e: Exception) {
                Logger.w(e) { "Failed to start game." }
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
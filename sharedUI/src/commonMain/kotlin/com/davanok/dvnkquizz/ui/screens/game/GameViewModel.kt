package com.davanok.dvnkquizz.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.repositories.GameSessionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class GameViewModel(
    @Assisted private val sessionId: Uuid,
    private val repository: GameSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameScreenUiState())
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        startHeartbeat()
    }

    private fun observeSession() {
        viewModelScope.launch {
            // 1. Convert the repository flow into a shared flow to avoid double-subscriptions
            val sessionFlow = repository.observeFullGameSession(sessionId).shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )

            // 2. Task A: Sync basic session data (Status, Host, Active Question)
            launch {
                sessionFlow.collect { result ->
                    result.onSuccess { data ->
                        _uiState.update { it.copy(
                            session = data.session,
                            isHost = data.isHost,
                            participants = data.participants
                        ) }
                    }.onFailure { thr ->
                        _uiState.update { it.copy(errorMessage = thr.message) }
                    }
                }
            }

            // 3. Task B: Sync the Board only when the Round ID actually changes
            launch {
                sessionFlow
                    .map { it.getOrNull()?.session?.currentRoundId }
                    .distinctUntilChanged() // Only proceeds if round ID is different from last emission
                    .collect { roundId ->
                        if (roundId == null) {
                            _uiState.update { it.copy(board = emptyList()) }
                        } else {
                            fetchBoard(roundId)
                        }
                    }
            }

            launch {
                sessionFlow
                    .map { it.getOrNull()?.session }
                    .filterNotNull()
                    .distinctUntilChangedBy { it.currentQuestionId to it.isAnswerVisible }
                    .collect { session ->
                        val sessionId = session.id
                        val questionId = session.currentQuestionId

                        if (questionId == null) {
                            _uiState.update { it.copy(currentQuestion = null) }
                        } else {
                            // Fetch the question details (RPC handles the redaction logic)
                            repository.getActiveQuestion(sessionId)
                                .onSuccess { questionData ->
                                    _uiState.update { it.copy(currentQuestion = questionData) }
                                }
                                .onFailure { thr ->
                                    _uiState.update { it.copy(errorMessage = thr.message) }
                                }
                        }
                    }
            }

            launch {
                sessionFlow
                    .map { it.getOrNull() }
                    .filterNotNull()
                    .collect { data ->
                        // Find the "Active Buzz": an answer record for the current question that hasn't been judged yet
                        val activeAnswer = data.answers.firstOrNull {
                            it.questionId == data.session.currentQuestionId && it.isCorrect == null
                        }

                        val buzzedParticipant = activeAnswer?.let { answer ->
                            data.participants.firstOrNull { it.id == answer.participantId }
                        }

                        _uiState.update { it.copy(
                            session = data.session,
                            isHost = data.isHost,
                            participants = data.participants,
                            buzzedParticipant = buzzedParticipant // Now the UI knows who is answering
                        ) }
                    }
            }
        }
    }
    private suspend fun fetchBoard(roundId: Uuid) {
        repository.getSessionBoard(sessionId, roundId)
            .onSuccess { board ->
                _uiState.update { it.copy(board = board) }
            }
            .onFailure { thr ->
                _uiState.update { it.copy(errorMessage = thr.message) }
            }
    }
    fun onQuestionClicked(questionId: Uuid) {
        viewModelScope.launch {
            repository.pickQuestion(sessionId, questionId)
        }
    }

    fun onBuzzIn() {
        viewModelScope.launch {
            repository.buzzIn(sessionId)
                .onFailure { thr ->
                    _uiState.update { it.copy(errorMessage = thr.message) }
                }
        }
    }
    fun onSkipQuestion() {
        viewModelScope.launch {
            repository.skipQuestion(sessionId)
        }
    }

    fun onJudge(participantId: Uuid, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.judgeAnswer(sessionId, participantId, isCorrect)
        }
    }

    private fun startHeartbeat() = viewModelScope.launch {
        while (this.isActive) {
            repository.sendHeartbeat(sessionId)
            delay(repository.HEARTBEAT_TIMEOUT)
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted sessionId: Uuid): GameViewModel
    }
}
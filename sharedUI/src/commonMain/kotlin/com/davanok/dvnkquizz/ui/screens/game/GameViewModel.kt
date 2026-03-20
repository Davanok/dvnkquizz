package com.davanok.dvnkquizz.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.repositories.GameProcessRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@AssistedInject
class GameViewModel(
    @Assisted private val sessionId: Uuid,
    private val repository: GameProcessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameScreenUiState>(GameScreenUiState.Loading)
    val uiState: StateFlow<GameScreenUiState> = _uiState.asStateFlow()

    private val _heartbeatJob: Job = startHeartbeat()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.observeGameSession(sessionId).collect { session ->
                session.mapCatching { it.toUiState() }.fold(
                    onFailure = { thr ->
                        _uiState.update {
                            GameScreenUiState.FatalError(thr.message.toString())
                        }
                    },
                    onSuccess = { state ->
                        _uiState.update { state }
                    }
                )
            }
        }
    }

    private fun FullGameSession.toUiState(): GameScreenUiState = when {
        session.status == SessionStatus.FINISHED -> GameScreenUiState.Results(
            isHost = isHost,
            participants = participants
        )

        session.currentRoundId == null -> GameScreenUiState.Idle(
            isHost = isHost,
            participants = participants
        )

        activeQuestion == null -> GameScreenUiState.SelectQuestion(
            isHost = isHost,
            participants = participants,
            board = gameBoard
        )

        answers.none { it.questionId == session.currentQuestionId } -> GameScreenUiState.Question(
            isHost = isHost,
            participants = participants,
            question = activeQuestion!!
        )

        answers.any { it.questionId == session.currentQuestionId && it.isCorrect == true } -> GameScreenUiState.Answer(
            isHost = isHost,
            participants = participants,
            question = activeQuestion!!
        )

        else -> {
            val activeAnswer = answers.first { it.questionId == session.currentQuestionId }
            val buzzedParticipant = participants.first {
                it.id == activeAnswer.participantId
            }
            GameScreenUiState.Answering(
                isHost = isHost,
                participants = participants,
                question = activeQuestion!!,
                buzzedParticipant = buzzedParticipant,
                isMe = buzzedParticipant.isMe
            )
        }
    }

    fun eventSink(event: GameScreenUiEvent) = viewModelScope.launch {
        runCatching {
            processEvent(event)
        }.onFailure { thr ->
            _uiState.update {
                it.copyState(message = thr.toString())
            }
        }
    }

    private suspend fun processEvent(event: GameScreenUiEvent) {
        val currentUiState = uiState.value

        when (event) {
            GameScreenUiEvent.NextRound -> {
                check(currentUiState.isHost) { "only host can start round" }

                repository.nextRound(sessionId)
            }
            GameScreenUiEvent.NextQuestion -> {
                check(currentUiState.isHost) { "only host can select another question" }

                repository.nextQuestion(sessionId)
            }

            is GameScreenUiEvent.SelectQuestion -> {
                check(currentUiState is GameScreenUiState.SelectQuestion)
                check(currentUiState.isHost) { "only host can select question" }

                repository.selectQuestion(sessionId, event.questionId)
            }

            GameScreenUiEvent.Buzz -> {
                check(currentUiState is GameScreenUiState.Question)
                check(!currentUiState.isHost) { "host cant buzz answer" }

                repository.buzzIn(sessionId)
            }

            is GameScreenUiEvent.JudgeAnswer -> {
                check(currentUiState is GameScreenUiState.Answering)
                check(currentUiState.isHost) { "only host can judge answer" }

                repository.judgeAnswer(sessionId, currentUiState.buzzedParticipant.id, event.isCorrect)
            }

            GameScreenUiEvent.Leave -> {
                _heartbeatJob.cancel()
            }
        }
    }

    private fun startHeartbeat() = viewModelScope.launch {
        while (this.isActive) {
            repository.sendHeartbeat(sessionId)
            delay(repository.HEARTBEAT_TIMEOUT_MS)
        }
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted sessionId: Uuid): GameViewModel
    }
}
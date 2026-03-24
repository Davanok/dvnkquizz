package com.davanok.dvnkquizz.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.Participant
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
import kotlinx.coroutines.flow.collectLatest
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
            repository.observeGameSession(sessionId).collectLatest { result ->
                _uiState.update {
                    result.fold(
                        onFailure = { thr ->
                            GameScreenUiState.FatalError(thr.message.toString())
                        },
                        onSuccess = { session ->
                            session.toUiState()
                        }
                    )
                }
            }
        }
    }

    private fun FullGameSession.toUiState(): GameScreenUiState {
        val participants = participants
            .sortedWith(
                compareBy<Participant> { it.role } // host is first, spectators in end
                    .thenByDescending { it.score } // higher score in start
            )

        return when {
            session.status == SessionStatus.FINISHED -> GameScreenUiState.Results(
                isHost = isHost,
                gamePackage = gamePackage,
                participants = participants
            )

            session.currentRoundId == null -> GameScreenUiState.Idle(
                isHost = isHost,
                gamePackage = gamePackage,
                participants = participants
            )

            activeQuestion == null -> GameScreenUiState.SelectQuestion(
                isHost = isHost,
                gamePackage = gamePackage,
                participants = participants,
                board = gameBoard.groupBy { it.categoryName }
            )

            else -> {
                val activeAnswers = answers.filter { it.questionId == session.currentQuestionId }

                when {
                    activeAnswers.none { it.isCorrect != false } -> GameScreenUiState.Question(
                        isHost = isHost,
                        gamePackage = gamePackage,
                        participants = participants,
                        showQuestionAt = session.showQuestionAt,
                        question = activeQuestion!!
                    )

                    activeAnswers.none { it.isCorrect == true } -> {
                        val answer = activeAnswers
                            .filter { it.isCorrect == null }
                            .minBy { it.answeredAt }

                        val participant = participants
                            .first { it.id == answer.participantId }

                        GameScreenUiState.Answering(
                            isHost = isHost,
                            gamePackage = gamePackage,
                            participants = participants,
                            question = activeQuestion!!,
                            answer = answer,
                            buzzedParticipant = participant
                        )
                    }

                    else -> {
                        val answer = activeAnswers
                            .filter { it.isCorrect == true }
                            .minBy { it.answeredAt }

                        val participant = participants
                            .first { it.id == answer.participantId }

                        GameScreenUiState.Answer(
                            isHost = isHost,
                            gamePackage = gamePackage,
                            participants = participants,
                            question = activeQuestion!!,
                            answer = answer,
                            answeredParticipant = participant
                        )
                    }
                }
            }
        }
    }

    fun eventSink(event: GameScreenUiEvent) = viewModelScope.launch {
        processEvent(event).onFailure { thr ->
            _uiState.update {
                it.copyState(message = thr.toString())
            }
        }
    }

    private suspend fun processEvent(event: GameScreenUiEvent): Result<Any> {
        val currentUiState = uiState.value

        return when (event) {
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

                repository.judgeAnswer(sessionId, event.answerId, event.isCorrect)
            }

            GameScreenUiEvent.Leave -> {
                runCatching {
                    _heartbeatJob.cancel()
                }
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
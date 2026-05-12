package com.davanok.dvnkquizz.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.game.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import com.davanok.dvnkquizz.core.domain.game.enums.ParticipantRole
import com.davanok.dvnkquizz.core.domain.game.enums.SessionStatus
import com.davanok.dvnkquizz.core.domain.game.repositories.GameProcessRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.only_host_can_judge_answer
import dvnkquizz.sharedui.generated.resources.only_host_can_select_question
import dvnkquizz.sharedui.generated.resources.only_host_can_start_round
import dvnkquizz.sharedui.generated.resources.only_player_can_buzz
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
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
            repository.observeGameSession(sessionId).collect { result ->
                Logger.d { "gameSessionUpdated: $result" }
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
                    .thenBy { it.id } // ignore random movements
            )

        val currentQuestion = activeQuestion

        return when {
            session.status == SessionStatus.FINISHED -> GameScreenUiState.Results(
                role = myRole,
                inviteCode = session.inviteCode,
                gamePackage = gamePackage,
                participants = participants.filter { it.role == ParticipantRole.PLAYER }
            )
            session.status == SessionStatus.LOBBY || session.currentRoundId == null -> GameScreenUiState.Idle(
                role = myRole,
                inviteCode = session.inviteCode,
                gamePackage = gamePackage,
                participants = participants
            )

            currentQuestion == null -> GameScreenUiState.SelectQuestion(
                role = myRole,
                inviteCode = session.inviteCode,
                gamePackage = gamePackage,
                participants = participants,
                board = gameBoard
                    .groupBy { it.categoryName }
                    .mapValues { (_, value) ->
                        value.sortedBy { it.price }
                    }
            )

            else -> {
                val activeAnswers = answers
                    .filter { it.questionId == session.currentQuestionId }

                when {
                    activeAnswers.none { it.isCorrect != false } -> GameScreenUiState.Question(
                        role = myRole,
                        inviteCode = session.inviteCode,
                        gamePackage = gamePackage,
                        participants = participants,
                        showQuestionAt = session.showQuestionAt,
                        question = currentQuestion
                    )

                    activeAnswers.none { it.isCorrect == true } -> {
                        val answer = activeAnswers
                            .filter { it.isCorrect == null }
                            .minBy { it.answeredAt }

                        val participant = participants
                            .first { it.id == answer.participantId }

                        GameScreenUiState.Answering(
                            role = myRole,
                            inviteCode = session.inviteCode,
                            gamePackage = gamePackage,
                            participants = participants,
                            question = currentQuestion,
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
                            role = myRole,
                            inviteCode = session.inviteCode,
                            gamePackage = gamePackage,
                            participants = participants,
                            question = currentQuestion,
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
                check(currentUiState.isHost) {
                    getString(Res.string.only_host_can_start_round)
                }

                repository.nextRound(sessionId)
            }
            GameScreenUiEvent.NextQuestion -> {
                check(currentUiState.isHost) { 
                    getString(Res.string.only_host_can_select_question)
                }

                repository.nextQuestion(sessionId)
            }

            is GameScreenUiEvent.SelectQuestion -> {
                check(currentUiState is GameScreenUiState.SelectQuestion)
                check(currentUiState.isHost) {
                    getString(Res.string.only_host_can_select_question)
                }

                repository.selectQuestion(sessionId, event.questionId)
            }

            is GameScreenUiEvent.Buzz -> {
                check(currentUiState is GameScreenUiState.Question)
                check(currentUiState.role == ParticipantRole.PLAYER) {
                    getString(Res.string.only_player_can_buzz)
                }

                repository.buzzIn(sessionId, event.answer)
            }

            is GameScreenUiEvent.JudgeAnswer -> {
                check(currentUiState is GameScreenUiState.Answering)
                check(currentUiState.isHost) { 
                    getString(Res.string.only_host_can_judge_answer)
                }

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
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(@Assisted sessionId: Uuid): GameViewModel
    }
}
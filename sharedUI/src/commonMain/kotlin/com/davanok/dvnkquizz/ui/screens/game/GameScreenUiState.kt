package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Immutable
sealed interface GameScreenUiState {
    val isHost: Boolean
    val inviteCode: String?
    val gamePackage: GamePackage?
    val participants: List<Participant>
    val message: String?

    fun copyState(
        message: String?
    ): GameScreenUiState

    data object Loading : GameScreenUiState {
        override val isHost: Boolean = false
        override val inviteCode: String? = null
        override val gamePackage: GamePackage? = null
        override val participants: List<Participant> = emptyList()
        override val message: String? = null

        override fun copyState(message: String?) = this
    }

    data class FatalError(override val message: String) : GameScreenUiState {
        override val isHost = false
        override val inviteCode: String? = null
        override val gamePackage: GamePackage? = null
        override val participants: List<Participant> = emptyList()

        override fun copyState(message: String?): GameScreenUiState = this
    }

    data class Idle(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class SelectQuestion(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val board: Map<String, List<GameBoardItem>>
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Question(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val showQuestionAt: Instant?,
        val question: com.davanok.dvnkquizz.core.domain.entities.Question
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Answering(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val question: com.davanok.dvnkquizz.core.domain.entities.Question,
        val answer: SessionAnswer,
        val buzzedParticipant: Participant,
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Answer(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val question: com.davanok.dvnkquizz.core.domain.entities.Question,
        val answer: SessionAnswer,
        val answeredParticipant: Participant,
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Results(
        override val isHost: Boolean,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }
}

sealed interface GameScreenUiEvent {
    // Control
    data object NextRound : GameScreenUiEvent
    data object NextQuestion : GameScreenUiEvent

    // GameBoard
    data class SelectQuestion(val questionId: Uuid) : GameScreenUiEvent

    // Question
    data object Buzz : GameScreenUiEvent

    // Answering
    data class JudgeAnswer(val answerId: Uuid, val isCorrect: Boolean) : GameScreenUiEvent

    // Results
    data object Leave : GameScreenUiEvent
}
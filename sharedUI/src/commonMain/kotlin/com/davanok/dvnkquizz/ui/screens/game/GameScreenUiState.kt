package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.game.entities.GameBoardRow
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.game.entities.Participant
import com.davanok.dvnkquizz.core.domain.game.entities.SessionAnswer
import com.davanok.dvnkquizz.core.domain.game.enums.ParticipantRole
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Immutable
sealed interface GameScreenUiState {
    val role: ParticipantRole
    val inviteCode: String?
    val gamePackage: GamePackage?
    val participants: List<Participant>
    val message: String?

    fun copyState(
        message: String?
    ): GameScreenUiState

    data object Loading : GameScreenUiState {
        override val role: ParticipantRole = ParticipantRole.SPECTATOR
        override val inviteCode: String? = null
        override val gamePackage: GamePackage? = null
        override val participants: List<Participant> = emptyList()
        override val message: String? = null

        override fun copyState(message: String?) = this
    }

    data class FatalError(override val message: String) : GameScreenUiState {
        override val role: ParticipantRole = ParticipantRole.SPECTATOR
        override val inviteCode: String? = null
        override val gamePackage: GamePackage? = null
        override val participants: List<Participant> = emptyList()

        override fun copyState(message: String?): GameScreenUiState = this
    }

    data class Idle(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val startEnabled: Boolean
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class SelectQuestion(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val board: List<GameBoardRow>
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Question(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val showQuestionAt: Instant?,
        val question: com.davanok.dvnkquizz.core.domain.game.entities.Question
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Answering(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val question: com.davanok.dvnkquizz.core.domain.game.entities.Question,
        val answer: SessionAnswer,
        val buzzedParticipant: Participant,
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Answer(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null,

        val question: com.davanok.dvnkquizz.core.domain.game.entities.Question,
        val answer: SessionAnswer,
        val answeredParticipant: Participant,
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }

    data class Results(
        override val role: ParticipantRole,
        override val inviteCode: String?,
        override val gamePackage: GamePackage?,
        override val participants: List<Participant>,
        override val message: String? = null
    ) : GameScreenUiState {
        override fun copyState(message: String?): GameScreenUiState = copy(message = message)
    }
}


val GameScreenUiState.isHost: Boolean
    get() = role == ParticipantRole.HOST

sealed interface GameScreenUiEvent {
    // Control
    data object NextRound : GameScreenUiEvent
    data object NextQuestion : GameScreenUiEvent

    // GameBoard
    data class SelectQuestion(val questionId: Uuid) : GameScreenUiEvent

    // Question
    data class Buzz(val answer: String) : GameScreenUiEvent

    // Answering
    data class JudgeAnswer(val answerId: Uuid, val isCorrect: Boolean) : GameScreenUiEvent

    // Results
    data object Leave : GameScreenUiEvent
}
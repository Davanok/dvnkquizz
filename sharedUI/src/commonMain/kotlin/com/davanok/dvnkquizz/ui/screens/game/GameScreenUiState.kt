package com.davanok.dvnkquizz.ui.screens.game

import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.Participant
import com.davanok.dvnkquizz.core.domain.entities.Question

data class GameScreenUiState(
    val session: GameSession? = null,
    val isHost: Boolean = false,
    val participants: List<Participant> = emptyList(),
    val board: List<GameBoardItem> = emptyList(),
    val currentQuestion: Question? = null,
    val buzzedParticipant: Participant? = null, // Calculated below
    val errorMessage: String? = null
)
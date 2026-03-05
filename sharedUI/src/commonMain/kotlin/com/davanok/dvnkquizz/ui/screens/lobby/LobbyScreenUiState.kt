package com.davanok.dvnkquizz.ui.screens.lobby

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.GameSession
import com.davanok.dvnkquizz.core.domain.entities.Participant

@Immutable
data class LobbyScreenUiState(
    val isLoading: Boolean = true,
    val session: GameSession? = null,
    val participants: List<Participant> = emptyList(),
    val isHost: Boolean = false,
    val errorMessage: String? = null
)
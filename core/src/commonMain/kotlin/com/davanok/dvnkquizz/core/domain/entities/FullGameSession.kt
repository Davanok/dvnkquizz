package com.davanok.dvnkquizz.core.domain.entities

data class FullGameSession(
    val session: GameSession,
    val isHost: Boolean,
    val gamePackage: GamePackage,
    val participants: List<Participant>,
    val answers: List<SessionAnswer>,
    val gameBoard: List<GameBoardItem>,
    val activeQuestion: Question?
)
package com.davanok.dvnkquizz.core.domain.entities

data class FullGameSession(
    val session: GameSession,
    val isHost: Boolean,
    val participants: List<Participant>,
    val answers: List<SessionAnswer>
)
package com.davanok.dvnkquizz.core.domain.entities

data class GameSessionStatus(
    val session: GameSession,
    val gamePackage: GamePackage,
    val isHost: Boolean,
    val participants: List<Participant>
)

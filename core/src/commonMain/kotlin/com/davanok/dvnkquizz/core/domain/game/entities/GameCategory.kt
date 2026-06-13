package com.davanok.dvnkquizz.core.domain.game.entities

import kotlin.uuid.Uuid

data class GameCategory(
    val id: Uuid = Uuid.random(),
    val roundId: Uuid,
    val name: String = "",
    val ordinal: Int = 0
)
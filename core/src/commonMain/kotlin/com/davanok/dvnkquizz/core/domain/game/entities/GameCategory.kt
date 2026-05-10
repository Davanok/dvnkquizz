package com.davanok.dvnkquizz.core.domain.game.entities

import kotlin.uuid.Uuid

data class GameCategory(
    val id: Uuid = Uuid.random(),
    val name: String = "",
    val ordinal: Int = 0
)
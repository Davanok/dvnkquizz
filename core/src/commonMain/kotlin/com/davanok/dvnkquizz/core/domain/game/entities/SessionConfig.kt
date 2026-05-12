package com.davanok.dvnkquizz.core.domain.game.entities

import kotlinx.serialization.Serializable

@Serializable
data class SessionConfig(
    val textAnswer: Boolean
)

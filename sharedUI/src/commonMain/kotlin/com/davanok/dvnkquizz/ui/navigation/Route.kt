package com.davanok.dvnkquizz.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route
    @Serializable
    data class Lobby(val sessionId: Uuid) : Route

    @Serializable
    data class Game(val sessionId: Uuid) : Route
}
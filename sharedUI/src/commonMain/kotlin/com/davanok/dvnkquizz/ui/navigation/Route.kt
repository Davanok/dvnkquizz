package com.davanok.dvnkquizz.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

interface RequiresAuth

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object About: Route
    @Serializable
    data object Auth : Route

    @Serializable
    data object Home : Route, RequiresAuth

    @Serializable
    data class Game(val sessionId: Uuid) : Route, RequiresAuth

    @Serializable
    data object UserGamePackages : Route, RequiresAuth
    @Serializable
    data class EditUserGamePackage(val packageId: Uuid?) : Route, RequiresAuth
}
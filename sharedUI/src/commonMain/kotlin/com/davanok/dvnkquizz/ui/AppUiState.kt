package com.davanok.dvnkquizz.ui

import com.davanok.dvnkquizz.core.domain.entities.User
import com.davanok.dvnkquizz.ui.navigation.Route

data class AppUiState(
    val user: User? = null,
    val errorMessage: String? = null,
    val backStack: List<Route> = listOf(Route.PlaceHolder)
)

sealed interface NavigationEvent {
    data object Back: NavigationEvent
    data class Navigate(val route: Route): NavigationEvent
    data class Replace(val route: Route): NavigationEvent
}
package com.davanok.dvnkquizz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.davanok.dvnkquizz.ui.screens.auth.AuthScreen
import com.davanok.dvnkquizz.ui.screens.game.GameScreen
import com.davanok.dvnkquizz.ui.screens.game.GameViewModel
import com.davanok.dvnkquizz.ui.screens.home.HomeScreen
import com.davanok.dvnkquizz.ui.screens.lobby.LobbyScreen
import com.davanok.dvnkquizz.ui.screens.lobby.LobbyViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun AppNavDisplay(
    backStack: List<Route>,
    navigate: (Route) -> Unit,
    back: () -> Unit,
    replace: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = back,
        entryDecorators = navEntryDecorators(),
        entryProvider = entryProvider {
            entry<Route.PlaceHolder> {
                PlaceholderScreen()
            }
            entry<Route.Auth> {
                AuthScreen()
            }
            entry<Route.Home> {
                HomeScreen(
                    onNavigateToLobby = { sessionId ->
                        val route = Route.Lobby(sessionId)
                        navigate(route)
                    }
                )
            }
            entry<Route.Lobby> { (sessionId) ->
                LobbyScreen(
                    onNavigateToGame = { replace(Route.Game(it)) },
                    viewModel = assistedMetroViewModel<LobbyViewModel, LobbyViewModel.Factory>(key = sessionId.toString()) { create(sessionId) }
                )
            }
            entry<Route.Game> { (sessionId) ->
                GameScreen(
                    viewModel = assistedMetroViewModel<GameViewModel, GameViewModel.Factory>(key = sessionId.toString()) { create(sessionId) }
                )
            }
        }
    )
}
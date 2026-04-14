package com.davanok.dvnkquizz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.davanok.dvnkquizz.ui.screens.auth.AuthScreen
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageScreen
import com.davanok.dvnkquizz.ui.screens.editGamePackage.EditGamePackageViewModel
import com.davanok.dvnkquizz.ui.screens.game.GameScreen
import com.davanok.dvnkquizz.ui.screens.game.GameViewModel
import com.davanok.dvnkquizz.ui.screens.home.HomeScreen
import com.davanok.dvnkquizz.ui.screens.userGamePackages.UserGamePackagesScreen
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
                    onNavigateToLobby = { sessionId -> navigate(Route.Game(sessionId)) },
                    navigateToUserPackages = { navigate(Route.UserGamePackages) }
                )
            }
            entry<Route.Game> { (sessionId) ->
                GameScreen(
                    navigateBack = back,
                    viewModel = assistedMetroViewModel<GameViewModel, GameViewModel.Factory>(key = sessionId.toString()) { create(sessionId) }
                )
            }

            entry<Route.UserGamePackages> {
                UserGamePackagesScreen(
                    onBackClick = back,
                    navigateToPackage = { packageId -> navigate(Route.EditUserGamePackage(packageId)) },
                    navigateToNewPackage = { navigate(Route.EditUserGamePackage(null)) }
                )
            }
            entry<Route.EditUserGamePackage> { (packageId) ->
                EditGamePackageScreen(
                    viewModel = assistedMetroViewModel<EditGamePackageViewModel, EditGamePackageViewModel.Factory>(key = packageId.toString()) { create(packageId) },
                    navigateBack = back
                )
            }
        }
    )
}
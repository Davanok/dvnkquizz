package com.davanok.dvnkquizz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.davanok.dvnkquizz.ui.screens.aboutScreen.AboutScreen
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
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    val backStack: SnapshotStateList<Route> = remember {
        mutableStateListOf(if (isLoggedIn) Route.Home else Route.About)
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            backStack.removeAll { it is RequiresAuth }
            if (backStack.isEmpty()) backStack.add(Route.About)
        }
    }

    val navigate: (route: Route) -> Unit by rememberUpdatedState {
        if (isLoggedIn || it !is RequiresAuth)
            backStack.add(it)
        else
            backStack.add(Route.Auth)
    }
    val back: () -> Unit by rememberUpdatedState {
        if (backStack.size > 1) backStack.removeLastOrNull()
        else backStack[0] = Route.About
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = back,
        entryDecorators = navEntryDecorators(),
        entryProvider = entryProvider {
            entry<Route.About> {
                AboutScreen(
                    navigateNext = { navigate(Route.Home) }
                )
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
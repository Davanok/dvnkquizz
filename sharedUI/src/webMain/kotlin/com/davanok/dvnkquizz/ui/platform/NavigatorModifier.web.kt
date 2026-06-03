package com.davanok.dvnkquizz.ui.platform

import androidx.compose.runtime.Composable
import com.davanok.dvnkquizz.ui.navigation.Route
import com.github.terrakok.navigation3.browser.HierarchicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment

@Composable
actual fun NavigatorModifier(currentRoute: Route) {
    HierarchicalBrowserNavigation {
        when (currentRoute) {
            Route.PlaceHolder -> buildBrowserHistoryFragment("placeholder")
            Route.Auth -> buildBrowserHistoryFragment("auth")
            is Route.EditUserGamePackage -> buildBrowserHistoryFragment(
                "edit-game-package",
                mapOf("package-id" to currentRoute.packageId.toString())
            )
            is Route.Game -> buildBrowserHistoryFragment(
                "game",
                mapOf("session-id" to currentRoute.sessionId.toString())
            )
            Route.Home -> buildBrowserHistoryFragment("home")
            Route.UserGamePackages -> buildBrowserHistoryFragment("game-packages")
        }
    }
}
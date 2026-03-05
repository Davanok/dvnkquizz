package com.davanok.dvnkquizz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.davanok.dvnkquizz.ui.navigation.AppNavDisplay
import com.davanok.dvnkquizz.ui.navigation.Route
import com.davanok.dvnkquizz.ui.theme.AppTheme
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@Inject
class AppClass(
    private val metroViewModel: MetroViewModelFactory,
) {
    @Composable
    operator fun invoke(onThemeChanged: (Boolean) -> Unit = {}) =
        CompositionLocalProvider(LocalMetroViewModelFactory provides metroViewModel) {
            AppTheme(onThemeChanged) {
                AppNavDisplay(
                    startDestination = Route.Home,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
}
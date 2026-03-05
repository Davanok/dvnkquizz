package com.davanok.dvnkquizz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.davanok.dvnkquizz.theme.AppTheme
import com.davanok.dvnkquizz.ui.screens.lobby.LobbyScreen
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
                LobbyScreen()
            }
        }
}
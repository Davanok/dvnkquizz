package com.davanok.dvnkquizz.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.dvnkquizz.ui.navigation.AppNavDisplay
import com.davanok.dvnkquizz.ui.theme.AppTheme
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Inject
class AppClass(
    private val metroViewModel: MetroViewModelFactory,
) {
    @Composable
    operator fun invoke(onThemeChanged: (Boolean) -> Unit = {}) {
        CompositionLocalProvider(LocalMetroViewModelFactory provides metroViewModel) {
            val viewModel: AppViewModel = metroViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AppTheme(
                uiState.theme,
                onThemeChanged
            ) {
                if (uiState.isLoading) {
                    PlaceholderScreen(Modifier.fillMaxSize())
                }
                else {
                    val isLoggedIn by derivedStateOf { uiState.user != null }
                    AppNavDisplay(
                        isLoggedIn = isLoggedIn,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
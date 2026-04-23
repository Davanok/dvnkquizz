package com.davanok.dvnkquizz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.davanok.dvnkquizz.core.domain.enums.AppTheme
import com.davanok.dvnkquizz.ui.LocalSnackBarHostState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppTheme(
    mode: AppTheme,
    onThemeChanged: (isDark: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val systemIsDark = when(mode) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    LaunchedEffect(systemIsDark) {
        onThemeChanged(systemIsDark)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackBarHostState provides snackbarHostState) {
        MaterialTheme(
            colorScheme = if (systemIsDark) darkColorScheme() else expressiveLightColorScheme(),
            content = {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        content()
                    }
                }
            }
        )
    }
}

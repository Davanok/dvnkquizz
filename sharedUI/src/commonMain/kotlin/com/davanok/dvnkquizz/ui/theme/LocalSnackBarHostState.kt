package com.davanok.dvnkquizz.ui.theme

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackBarHostState not provided")
}
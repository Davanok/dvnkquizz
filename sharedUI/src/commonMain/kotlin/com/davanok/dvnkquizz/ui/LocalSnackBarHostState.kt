package com.davanok.dvnkquizz.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackBarHostState not provided")
}
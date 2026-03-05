package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.runtime.Immutable

@Immutable
sealed class HomeScreenUiState {
    object Idle : HomeScreenUiState()
    object Loading : HomeScreenUiState()
    data class Error(val message: String) : HomeScreenUiState()
}
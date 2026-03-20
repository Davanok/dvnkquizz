package com.davanok.dvnkquizz.ui.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()

}
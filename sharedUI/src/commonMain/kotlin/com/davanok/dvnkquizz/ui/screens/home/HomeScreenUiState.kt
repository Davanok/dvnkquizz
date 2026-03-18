package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
data class HomeScreenUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nickname: String = "",
    val image: Uuid? = null
)
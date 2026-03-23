package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.runtime.Immutable

@Immutable
data class HomeScreenUiState(
    val isSessionLoading: Boolean = false,
    val errorMessage: String? = null,
    val isProfileLoading: Boolean = true,
    val nickname: String = "",
    val imageUrl: String? = null,
    val nicknameChanged: Boolean = false
)
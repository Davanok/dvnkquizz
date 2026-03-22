package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.ui.domain.ImageStatus

@Immutable
data class HomeScreenUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nickname: String = "",
    val image: ImageStatus? = ImageStatus.Loading(),
    val nicknameChanged: Boolean = false
)
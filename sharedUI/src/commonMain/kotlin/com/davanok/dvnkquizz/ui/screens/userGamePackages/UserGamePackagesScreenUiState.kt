package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.GamePackage

@Immutable
data class UserGamePackagesScreenUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val gamePackages: List<GamePackage> = emptyList()
)
package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.GamePackage

@Immutable
data class UserGamePackagesScreenUiState(
    val isLoading: Boolean,
    val errorMessage: String,
    val gamePackages: List<GamePackage>
)
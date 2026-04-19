package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.compose.runtime.Immutable
import com.davanok.dvnkquizz.core.domain.entities.GamePackage

@Immutable
data class UserGamePackagesScreenUiState(
    val isExternalLoading: Boolean = true,
    val external: List<GamePackage> = emptyList(),
    val externalError: String? = null,
    val isDraftsLoading: Boolean = true,
    val drafts: List<GamePackage> = emptyList(),
    val draftsError: String? = null
)
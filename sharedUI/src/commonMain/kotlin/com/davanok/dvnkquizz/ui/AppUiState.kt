package com.davanok.dvnkquizz.ui

import com.davanok.dvnkquizz.core.domain.auth.entities.User
import com.davanok.dvnkquizz.core.domain.settings.enums.AppTheme

data class AppUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val theme: AppTheme = AppTheme.entries.first(),
    val isFirstTime: Boolean = true,
    val errorMessage: String? = null
)
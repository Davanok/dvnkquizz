package com.davanok.dvnkquizz.core.domain.settings.entities

import com.davanok.dvnkquizz.core.domain.settings.enums.AppTheme

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val isFirstTime: Boolean = true
)
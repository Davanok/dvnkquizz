package com.davanok.dvnkquizz.core.domain.settings.entities

import com.davanok.dvnkquizz.core.domain.settings.enums.AppTheme
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM
)
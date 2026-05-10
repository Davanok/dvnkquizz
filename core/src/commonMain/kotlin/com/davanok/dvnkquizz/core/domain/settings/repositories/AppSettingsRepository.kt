package com.davanok.dvnkquizz.core.domain.settings.repositories

import com.davanok.dvnkquizz.core.domain.settings.entities.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>
    suspend fun updateAppSettings(appSettings: AppSettings)
}
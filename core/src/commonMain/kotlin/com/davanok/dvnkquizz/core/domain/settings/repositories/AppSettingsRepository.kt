package com.davanok.dvnkquizz.core.domain.settings.repositories

import com.davanok.dvnkquizz.core.domain.settings.entities.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    suspend fun getAppSettings(): AppSettings
    fun observeAppSettings(): Flow<AppSettings>
    suspend fun setAppSettings(appSettings: AppSettings)
    suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings)
}
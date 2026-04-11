package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.BuildConfig
import com.davanok.dvnkquizz.core.domain.entities.AppSettings
import com.davanok.dvnkquizz.core.domain.repositories.SettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toSuspendSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


@Inject
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl(
    private val settings: ObservableSettings,
    logger: Logger
): SettingsRepository {
    private val logger = logger.withTag("SettingsRepository")
    private val serializer = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
    }


    @OptIn(ExperimentalSettingsApi::class)
    override fun observeAppSettings(): Flow<AppSettings> =
        settings.getStringFlow(APP_SETTINGS_KEY, "{}")
            .map { json ->
                runCatching {
                    serializer.decodeFromString(AppSettings.serializer(), json)
                }.onFailure {
                    logger.w(it) { "Failed to parse settings json ($json)" }
                }.getOrDefault(AppSettings())
            }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun updateAppSettings(settings: AppSettings) {
        this.settings
            .toSuspendSettings()
            .putString(
                APP_SETTINGS_KEY,
                serializer.encodeToString(
                    AppSettings.serializer(),
                    settings
                )
            )
    }

    companion object {
        private val APP_SETTINGS_KEY = "${BuildConfig.APP_ID}:app_settings"
    }
}
package com.davanok.dvnkquizz.core.data

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.BuildConfig
import com.davanok.dvnkquizz.core.domain.entities.AppSettings
import com.davanok.dvnkquizz.core.domain.repositories.SettingsRepository
import com.davanok.dvnkquizz.core.utils.SettingsUtils.getObjectFlow
import com.davanok.dvnkquizz.core.utils.SettingsUtils.putObject
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toSuspendSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch


@Inject
@ContributesBinding(AppScope::class)
class SettingsRepositoryImpl(
    private val settings: ObservableSettings,
    logger: Logger
): SettingsRepository {
    private val logger = logger.withTag("SettingsRepository")


    @OptIn(ExperimentalSettingsApi::class)
    override fun observeAppSettings(): Flow<AppSettings> =
        settings.getObjectFlow(APP_SETTINGS_KEY, AppSettings())
            .catch {
                logger.w(it) { "Failed to get app settings flow" }
                emit(AppSettings())
            }

    @OptIn(ExperimentalSettingsApi::class)
    override suspend fun updateAppSettings(appSettings: AppSettings) {
        settings
            .toSuspendSettings()
            .putObject(
                APP_SETTINGS_KEY,
                appSettings
            )
    }

    companion object {
        private const val APP_SETTINGS_KEY = "${BuildConfig.APP_ID}:app_settings"
    }
}
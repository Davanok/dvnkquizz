package com.davanok.dvnkquizz.core.data.storage

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.settings.entities.AppSettings
import com.davanok.dvnkquizz.core.domain.settings.enums.AppTheme
import com.davanok.dvnkquizz.core.domain.settings.repositories.AppSettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSettingsApi::class)
@Inject
@ContributesBinding(AppScope::class)
class AppSettingsRepositoryImpl(
    private val settings: FlowSettings,
    logger: Logger
) : AppSettingsRepository {
    private val logger = logger.withTag("SettingsRepository")

    private inline fun <reified T: Enum<T>> enumValueOfOrNull(name: String): T? =
        runCatching { enumValueOf<T>(name) }.getOrNull()

    private inline fun <reified T: Enum<T>> FlowSettings.getEnumFlow(key: String, defaultValue: T): Flow<T> =
        getStringOrNullFlow(key).map { str -> str?.let { enumValueOfOrNull(it) } ?: defaultValue }

    private suspend inline fun <reified T: Enum<T>> SuspendSettings.getEnum(key: String, defaultValue: T): T =
        getStringOrNull(key)?.let { enumValueOfOrNull(it) } ?: defaultValue

    override suspend fun getAppSettings(): AppSettings {
        val theme = settings.getEnum<AppTheme>(AppSettings::theme.name, AppTheme.entries.first())
        val isFirstTime = settings.getBoolean(AppSettings::isFirstTime.name, true)

        return AppSettings(
            theme = theme,
            isFirstTime = isFirstTime
        )
    }


    override fun observeAppSettings(): Flow<AppSettings> {
        val themeFlow = settings.getEnumFlow(AppSettings::theme.name, AppTheme.entries.first())
        val isFirstTimeFlow = settings.getBooleanFlow(AppSettings::isFirstTime.name, true)

        return combine(
            themeFlow,
            isFirstTimeFlow
        ) { theme, isFirstTime ->
            AppSettings(
                theme = theme,
                isFirstTime = isFirstTime
            )
        }
    }

    override suspend fun setAppSettings(appSettings: AppSettings) {
        settings.putBoolean(AppSettings::isFirstTime.name, appSettings.isFirstTime)
        settings.putString(AppSettings::theme.name, appSettings.theme.name)
    }

    override suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        val initial = getAppSettings()
        val modified = transform(initial)
        if (initial != modified) setAppSettings(modified)
    }
}
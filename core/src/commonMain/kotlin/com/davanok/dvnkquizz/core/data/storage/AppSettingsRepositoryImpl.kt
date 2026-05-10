package com.davanok.dvnkquizz.core.data.storage

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.settings.entities.AppSettings
import com.davanok.dvnkquizz.core.domain.settings.enums.AppTheme
import com.davanok.dvnkquizz.core.domain.settings.repositories.AppSettingsRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
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
): AppSettingsRepository {
    private val logger = logger.withTag("SettingsRepository")

    override fun observeAppSettings(): Flow<AppSettings> {
        val themeFlow = settings.getStringFlow(
            AppSettings::theme.name,
            AppTheme.entries.first().name
        ).map { AppTheme.valueOf(it) }

        return combine(themeFlow) { (theme) ->
            AppSettings(
                theme = theme
            )
        }
    }

    override suspend fun updateAppSettings(appSettings: AppSettings) {
        settings.putString(
            AppSettings::theme.name,
            appSettings.theme.name
        )
    }
}
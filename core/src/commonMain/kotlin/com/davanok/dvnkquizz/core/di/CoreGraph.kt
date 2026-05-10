package com.davanok.dvnkquizz.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.davanok.dvnkquizz.core.BuildConfig
import com.davanok.dvnkquizz.core.data.logging.FileLogWriter
import com.davanok.dvnkquizz.core.data.logging.RemoteLogWriter
import com.davanok.dvnkquizz.core.platform.Platform
import com.davanok.dvnkquizz.core.platform.currentPlatform
import com.davanok.dvnkquizz.core.core.logging.LOG_SEVERITY
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.observable.makeObservable
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json

interface CoreGraph: PlatformGraph {

    @Provides
    @SingleIn(AppScope::class)
    fun provideSupabase(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)

        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideAuth(supabase: SupabaseClient): Auth = supabase.auth
    @Provides
    @SingleIn(AppScope::class)
    fun providePostgres(supabase: SupabaseClient): Postgrest = supabase.postgrest
    @Provides
    @SingleIn(AppScope::class)
    fun provideRealtime(supabase: SupabaseClient): Realtime = supabase.realtime
    @Provides
    @SingleIn(AppScope::class)
    fun provideStorage(supabase: SupabaseClient): Storage = supabase.storage

    @OptIn(ExperimentalSettingsApi::class)
    @Provides
    @SingleIn(AppScope::class)
    fun provideSettings(): FlowSettings = Settings().makeObservable().toFlowSettings()

    @Provides
    @SingleIn(AppScope::class)
    fun provideLogger(
        postgrest: Postgrest
    ): Logger {
        val logWriters = mutableListOf(
            platformLogWriter(),
            RemoteLogWriter(Severity.Warn, postgrest)
        )
        if (Platform.currentPlatform() is Platform.Jvm)
            logWriters.add(FileLogWriter(provideLogsDir()))

        return Logger(
            mutableLoggerConfigInit(
                logWriters = logWriters.toTypedArray(),
                minSeverity = BuildConfig.LOG_SEVERITY
            )
        )
    }
}
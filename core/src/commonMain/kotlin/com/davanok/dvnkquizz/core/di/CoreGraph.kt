package com.davanok.dvnkquizz.core.di

import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.BuildConfig
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
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
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

interface CoreGraph {
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
    @Provides
    @SingleIn(AppScope::class)
    fun provideLogger(): Logger = Logger

    @OptIn(ExperimentalSettingsApi::class)
    @Provides
    @SingleIn(AppScope::class)
    fun provideUiSettings(): ObservableSettings = Settings().makeObservable()
}
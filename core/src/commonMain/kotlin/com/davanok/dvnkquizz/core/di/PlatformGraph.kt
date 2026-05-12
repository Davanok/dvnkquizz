package com.davanok.dvnkquizz.core.di

import com.davanok.dvnkquizz.core.data.storage.Storage
import io.ktor.client.engine.HttpClientEngine
import kotlinx.io.files.Path

expect interface PlatformGraph {
    open fun provideDataDir(): Path
    open fun provideTempDir(): Path
    open fun provideLogsDir(): Path

    open fun provideGamePackageDraftsStorage(): Storage

    open fun provideHttpClientEngine(): HttpClientEngine
}
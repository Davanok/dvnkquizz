package com.davanok.dvnkquizz.core.di

import com.davanok.dvnkquizz.core.data.storage.WebStorage
import com.davanok.dvnkquizz.core.data.storage.Storage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

actual interface PlatformGraph {

    @Named(name = "dataDir")
    @Provides
    actual fun provideDataDir(): Path = Path("")

    @Named(name = "tempDir")
    @Provides
    actual fun provideTempDir(): Path = Path("")

    @Named(name = "logsDir")
    @Provides
    actual fun provideLogsDir(): Path = Path("")

    @Provides
    @SingleIn(scope = AppScope::class)
    actual fun provideGamePackageDraftsStorage(): Storage =
        WebStorage(prefix = "drafts", format = Json)
}
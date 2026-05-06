package com.davanok.dvnkquizz.core.di

import com.davanok.dvnkquizz.core.data.StorageStorage
import com.davanok.dvnkquizz.core.domain.repositories.Storage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

actual interface PlatformGraph {

    @Named(name = "dataDir")
    @Provides
    actual fun provideDataDir(): Path = error("Web platform does not have access to files")

    @Named(name = "tempDir")
    @Provides
    actual fun provideTempDir(): Path = error("Web platform does not have access to files")

    @Named(name = "logsDir")
    @Provides
    actual fun provideLogsDir(): Path = error("Web platform does not have access to files")

    @Provides
    @SingleIn(scope = AppScope::class)
    actual fun provideGamePackageDraftsStorage(): Storage =
        StorageStorage(prefix = "drafts", format = Json)
}
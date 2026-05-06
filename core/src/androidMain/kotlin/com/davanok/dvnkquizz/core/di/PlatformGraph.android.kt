package com.davanok.dvnkquizz.core.di

import android.content.Context
import com.davanok.dvnkquizz.core.data.FilesStorage
import com.davanok.dvnkquizz.core.domain.repositories.Storage
import com.davanok.dvnkquizz.core.utils.div
import com.davanok.dvnkquizz.core.utils.toPath
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

actual interface PlatformGraph {
    val context: Context

    @Named(name = "dataDir")
    @Provides
    actual fun provideDataDir(): Path = context.filesDir.absolutePath.toPath()

    @Named(name = "tempDir")
    @Provides
    actual fun provideTempDir(): Path = context.cacheDir.absolutePath.toPath()

    @Named(name = "logsDir")
    @Provides
    actual fun provideLogsDir(): Path = provideDataDir() / "logs"

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @SingleIn(scope = AppScope::class)
    actual fun provideGamePackageDraftsStorage(): Storage =
        FilesStorage(dataDir = provideDataDir() / "drafts", format = ProtoBuf, "binpb")
}
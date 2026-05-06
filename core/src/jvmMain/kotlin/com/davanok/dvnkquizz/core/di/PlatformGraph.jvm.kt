package com.davanok.dvnkquizz.core.di

import ca.gosyer.appdirs.AppDirs
import com.davanok.dvnkquizz.core.BuildConfig
import com.davanok.dvnkquizz.core.data.FilesStorage
import com.davanok.dvnkquizz.core.domain.repositories.Storage
import com.davanok.dvnkquizz.core.utils.div
import com.davanok.dvnkquizz.core.utils.toPath
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

actual interface PlatformGraph {
    val appDirs: AppDirs
        get() = AppDirs { appName = BuildConfig.APP_NAME }

    @Named(name = "dataDir")
    @Provides
    actual fun provideDataDir(): Path = appDirs.getUserDataDir().toPath()

    @Named(name = "tempDir")
    @Provides
    actual fun provideTempDir(): Path = SystemTemporaryDirectory

    @Named(name = "logsDir")
    @Provides
    actual fun provideLogsDir(): Path = appDirs.getUserLogDir().toPath()

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @SingleIn(scope = AppScope::class)
    actual fun provideGamePackageDraftsStorage(): Storage =
        FilesStorage(dataDir = provideDataDir() / "drafts", format = ProtoBuf, "binpb")
}
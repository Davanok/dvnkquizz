package com.davanok.dvnkquizz.core.di

import com.davanok.dvnkquizz.core.data.FilesStorage
import com.davanok.dvnkquizz.core.domain.repositories.Storage
import com.davanok.dvnkquizz.core.utils.div
import com.davanok.dvnkquizz.core.utils.toPath
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual interface PlatformGraph {
    val fileManager: NSFileManager
        get() = NSFileManager.defaultManager

    @Named("dataDir")
    @Provides
    actual fun provideDataDir(): Path =
        fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            appropriateForURL = null,
            create = false,
            inDomain = NSUserDomainMask,
            error = null
        )!!.path!!.toPath()

    @Named("tempDir")
    @Provides
    actual fun provideTempDir(): Path =
        fileManager.URLForDirectory(
            directory = NSCachesDirectory,
            appropriateForURL = null,
            create = false,
            inDomain = NSUserDomainMask,
            error = null
        )!!.path!!.toPath()

    @Named("logsDir")
    @Provides
    actual fun provideLogsDir(): Path = provideDataDir() / "logs"

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @SingleIn(scope = AppScope::class)
    actual fun provideGamePackageDraftsStorage(): Storage =
        FilesStorage(dataDir = provideDataDir() / "drafts", format = ProtoBuf, "binpb")
}
package com.davanok.dvnkquizz.core.di

import com.davanok.dvnkquizz.core.domain.repositories.Storage
import kotlinx.io.files.Path

expect interface PlatformGraph {
    open fun provideDataDir(): Path
    open fun provideTempDir(): Path
    open fun provideLogsDir(): Path

    open fun provideGamePackageDraftsStorage(): Storage
}
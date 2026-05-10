package com.davanok.dvnkquizz.core.core.logging

import co.touchlab.kermit.Severity
import com.davanok.dvnkquizz.core.BuildConfig

internal val BuildConfig.LOG_SEVERITY by lazy {
    Severity.entries.firstOrNull {
        it.name.equals(BuildConfig.LOG_LEVEL, ignoreCase = true)
    } ?: Severity.Warn
}
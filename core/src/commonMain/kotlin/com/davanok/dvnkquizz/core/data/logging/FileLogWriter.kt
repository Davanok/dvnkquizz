package com.davanok.dvnkquizz.core.data.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.buffered
import kotlinx.io.writeString
import kotlin.time.Clock

class FileLogWriter(
    private val logsDir: Path,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : LogWriter() {

    private val fs = SystemFileSystem
    private val logFile = Path(logsDir, "app.log")

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        scope.launch {
            runCatching {
                ensureFileExists()

                val timestamp = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())

                val logLine = buildString {
                    append("[")
                    append(timestamp)
                    append("] ")

                    append(severity.name)
                    append("/")

                    append(tag)
                    append(": ")

                    append(message)

                    if (throwable != null) {
                        append("\n")
                        append(throwable.stackTraceToString())
                    }

                    append("\n")
                }

                fs.sink(logFile, append = true).buffered().use { sink ->
                    sink.writeString(logLine)
                }
            }

        }
    }

    private fun ensureFileExists() {
        if (!fs.exists(logsDir))
            fs.createDirectories(logsDir)
    }
}
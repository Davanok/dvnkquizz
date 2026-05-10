package com.davanok.dvnkquizz.core.data.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.davanok.dvnkquizz.core.BuildConfig
import com.davanok.dvnkquizz.core.platform.Platform
import com.davanok.dvnkquizz.core.platform.currentPlatform
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class RemoteLogWriter(
    private val minSeverity: Severity,
    private val postgrest: Postgrest,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : LogWriter() {
    private val logChannel = Channel<LogObject>(capacity = Channel.UNLIMITED)

    init {
        startLogFlusher()
    }

    @Serializable
    private data class LogObject(
        val severity: Severity,
        val tag: String,
        val message: String,
        @SerialName("stack_trace")
        val stackTrace: String?,
        val platform: Platform = Platform.currentPlatform(),
        @SerialName("app_version")
        val appVersion: String = BuildConfig.APP_VERSION
    )

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity >= minSeverity

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        val dto = LogObject(
            severity = severity,
            message = message,
            tag = tag,
            stackTrace = throwable?.stackTraceToString()
        )
        logChannel.trySend(dto)
    }

    private fun startLogFlusher() {
        scope.launch {
            val batch = mutableListOf<LogObject>()

            while (isActive) {
                batch.add(logChannel.receive())

                while (batch.size < 50) {
                    val next = logChannel.tryReceive().getOrNull() ?: break
                    batch.add(next)
                }

                if (batch.isNotEmpty())
                    runCatching {
                        postgrest.from("logs").insert(batch)
                    }.onFailure { error ->
                        println("RemoteLogWriter failed to sync batch: ${error.message}")
                    }

                batch.clear()

                delay(3000)
            }
        }
    }
}
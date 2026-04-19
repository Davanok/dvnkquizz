package com.davanok.dvnkquizz.core.utils

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.SuspendSettings
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

internal object SettingsUtils {
    @OptIn(ExperimentalSerializationApi::class)
    private val format = ProtoBuf

    @OptIn(ExperimentalSerializationApi::class)
    @ExperimentalSettingsApi
    suspend fun <T> SuspendSettings.putObject(serializer: KSerializer<T>, key: String, value: T) =
        putString(
            key = key,
            value = format.encodeToHexString(
                serializer = serializer,
                value = value
            )
        )

    @OptIn(ExperimentalSerializationApi::class)
    @ExperimentalSettingsApi
    suspend fun <T> SuspendSettings.getObject(
        serializer: KSerializer<T>,
        key: String,
        defaultValue: T
    ): T = getString(
        key = key,
        defaultValue = format.encodeToHexString(
            serializer = serializer,
            value = defaultValue
        )
    ).let {
        format.decodeFromHexString(
            deserializer = serializer,
            hex = it
        )
    }
    @OptIn(ExperimentalSerializationApi::class)
    @ExperimentalSettingsApi
    suspend fun <T> SuspendSettings.getObjectOrNull(
        serializer: KSerializer<T>,
        key: String
    ): T? = getStringOrNull(
        key = key
    )?.let {
        format.decodeFromHexString(
            deserializer = serializer,
            hex = it
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    @ExperimentalSettingsApi
    suspend inline fun <reified T> SuspendSettings.putObject(key: String, value: T) = putObject(
        serializer = format.serializersModule.serializer(typeOf<T>()),
        key = key,
        value = value
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    @ExperimentalSettingsApi
    suspend inline fun <reified T> SuspendSettings.getObject(key: String, defaultValue: T): T =
        getObject(
            serializer = format.serializersModule.serializer(typeOf<T>()) as KSerializer<T>,
            key = key,
            defaultValue = defaultValue
        )
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    @ExperimentalSettingsApi
    suspend inline fun <reified T> SuspendSettings.getObjectOrNull(key: String): T? =
        getObjectOrNull(
            serializer = format.serializersModule.serializer(typeOf<T>()) as KSerializer<T>,
            key = key
        )

    @OptIn(ExperimentalSerializationApi::class)
    @ExperimentalSettingsApi
    fun <T> ObservableSettings.getObjectFlow(
        serializer: KSerializer<T>,
        key: String,
        defaultValue: T
    ): Flow<T> = getStringFlow(
        key = key,
        defaultValue = format.encodeToHexString(
            serializer = serializer,
            value = defaultValue
        )
    ).map {
        format.decodeFromHexString(
            deserializer = serializer,
            hex = it
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    @ExperimentalSettingsApi
    inline fun <reified T> ObservableSettings.getObjectFlow(
        key: String,
        defaultValue: T
    ): Flow<T> =
        getObjectFlow(
            serializer = format.serializersModule.serializer(typeOf<T>()) as KSerializer<T>,
            key = key,
            defaultValue = defaultValue
        )
}
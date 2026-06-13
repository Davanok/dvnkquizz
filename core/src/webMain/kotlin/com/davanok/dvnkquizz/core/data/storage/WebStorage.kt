package com.davanok.dvnkquizz.core.data.storage

import kotlinx.browser.localStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat

class WebStorage(
    private val prefix: String,
    private val format: StringFormat,
    private val storage: org.w3c.dom.Storage = localStorage
) : Storage {
    val listenerMap = mutableMapOf<String, MutableSet<(String) -> Unit>>()

    override fun <T : @Serializable Any> set(
        key: String,
        value: T,
        serializer: SerializationStrategy<T>
    ) {
        val fullKey = buildKey(key)

        val encodedValue = format.encodeToString(serializer, value)
        storage.setItem(
            key = fullKey,
            value = encodedValue
        )
        listenerMap[key]?.forEach { it.invoke(encodedValue) }
    }

    override fun <T : @Serializable Any> get(
        key: String,
        serializer: DeserializationStrategy<T>
    ): T? {
        val fullKey = buildKey(key)
        val raw = storage.getItem(fullKey) ?: return null

        return runCatching {
            format.decodeFromString(serializer, raw)
        }.getOrNull()
    }

    override fun delete(key: String) {
        storage.removeItem(buildKey(key))
    }

    private fun buildKey(key: String) = "$prefix:$key"

    override fun <T : @Serializable Any> observe(
        key: String,
        serializer: DeserializationStrategy<T>
    ): Flow<T> = callbackFlow {
        get(key, serializer)?.let { send(it) }

        val listener: (String) -> Unit = {
            val deserializedValue = format.decodeFromString(serializer, it)
            trySend(deserializedValue)
        }

        listenerMap.getOrPut(key, ::mutableSetOf).add(listener)
        awaitClose {
            listenerMap[key]?.remove(listener)
        }
    }.distinctUntilChanged()
}
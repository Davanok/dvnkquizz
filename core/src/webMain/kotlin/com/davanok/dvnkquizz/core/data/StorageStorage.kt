package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.repositories.Storage
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat


class StorageStorage(
    private val prefix: String,
    private val format: StringFormat,
    private val storage: org.w3c.dom.Storage = kotlinx.browser.localStorage
) : Storage {
    override fun <T : @Serializable Any> set(
        key: String,
        value: T,
        serializer: SerializationStrategy<T>
    ) {
        val fullKey = buildKey(key)

        storage.setItem(
            key = fullKey,
            value = format.encodeToString(serializer, value)
        )
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
}
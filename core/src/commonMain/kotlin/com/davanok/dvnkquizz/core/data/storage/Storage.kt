package com.davanok.dvnkquizz.core.data.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer

interface Storage {
    fun <T : @Serializable Any> set(key: String, value: T, serializer: SerializationStrategy<T>)
    fun <T : @Serializable Any> get(key: String, serializer: DeserializationStrategy<T>): T?
    fun delete(key: String)

    fun <T : @Serializable Any> observe(key: String, serializer: DeserializationStrategy<T>): Flow<T>
}

inline fun <reified T: @Serializable Any> Storage.set(key: String, value: T) =
    set(
        key = key,
        value = value,
        serializer = serializer<T>()
    )
inline fun <reified T: @Serializable Any> Storage.get(key: String): T? =
    get(
        key = key,
        serializer = serializer<T>()
    )
inline fun <reified T: @Serializable Any> Storage.observe(key: String): Flow<T> =
    observe(
        key = key,
        serializer = serializer<T>()
    )
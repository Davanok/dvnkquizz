package com.davanok.dvnkquizz.core.domain.repositories

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer

interface Storage {
    fun <T : @Serializable Any> set(key: String, value: T, serializer: SerializationStrategy<T>)
    fun <T : @Serializable Any> get(key: String, serializer: DeserializationStrategy<T>): T?
    fun delete(key: String)
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
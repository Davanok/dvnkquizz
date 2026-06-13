package com.davanok.dvnkquizz.core.data.storage

import com.davanok.dvnkquizz.core.core.filesystem.div
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.collections.mutableSetOf

class FilesStorage(
    private val dataDir: Path,
    private val format: BinaryFormat,
    private val filesExtension: String
) : Storage {
    val listenerMap = mutableMapOf<String, MutableSet<(ByteArray) -> Unit>>()

    init {
        if (!SystemFileSystem.exists(dataDir)) {
            SystemFileSystem.createDirectories(dataDir)
        }
    }

    override fun <T : @Serializable Any> set(
        key: String,
        value: T,
        serializer: SerializationStrategy<T>
    ) {
        val filename = dataDir / "$key.$filesExtension"

        val tempFilename = Path("$filename.tmp")
        val encodedValue = format.encodeToByteArray(
            serializer = serializer,
            value = value
        )
        SystemFileSystem.sink(tempFilename).buffered().use {
            it.write(source = encodedValue)
        }
        SystemFileSystem.atomicMove(tempFilename, filename)
        listenerMap[key]?.forEach { it.invoke(encodedValue) }
    }

    override fun <T : @Serializable Any> get(
        key: String,
        serializer: DeserializationStrategy<T>
    ): T? {
        val filename = dataDir / "$key.$filesExtension"

        if (!SystemFileSystem.exists(filename)) return null

        return try {
            SystemFileSystem.source(filename).buffered().use {
                val bytes = it.readByteArray()
                format.decodeFromByteArray(serializer, bytes)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun delete(key: String) {
        val filename = dataDir / "$key.$filesExtension"
        SystemFileSystem.delete(filename, mustExist = false)
    }

    override fun <T : @Serializable Any> observe(
        key: String,
        serializer: DeserializationStrategy<T>
    ): Flow<T> = callbackFlow {
        get(key, serializer)?.let { send(it) }

        val listener: (ByteArray) -> Unit = {
            val deserializedValue = format.decodeFromByteArray(serializer, it)
            trySend(deserializedValue)
        }

        listenerMap.getOrPut(key, ::mutableSetOf).add(listener)
        awaitClose {
            listenerMap[key]?.remove(listener)
        }
    }.distinctUntilChanged()
}
package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.repositories.Storage
import com.davanok.dvnkquizz.core.utils.div
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

class FilesStorage(
    private val dataDir: Path,
    private val format: BinaryFormat,
    private val filesExtension: String
) : Storage {

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
        SystemFileSystem.sink(tempFilename).buffered().use {
            it.write(
                source = format.encodeToByteArray(
                    serializer = serializer,
                    value = value
                )
            )
        }
        SystemFileSystem.atomicMove(tempFilename, filename)
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
}
package com.davanok.dvnkquizz.core.utils

import com.davanok.dvnkquizz.core.domain.enums.MediaKind

private object FileRegistry {
    private val imageMap = mapOf(
        "image/jpeg" to setOf("jpg", "jpeg"),
        "image/png" to setOf("png"),
        "image/webp" to setOf("webp")
    )

    private val audioMap = mapOf(
        "audio/mpeg" to setOf("mp3"),
        "audio/wav" to setOf("wav")
    )

    private val videoMap = mapOf(
        "video/mp4" to setOf("mp4"),
        "video/x-matroska" to setOf("mkv")
    )

    // Pre-merge for performance: O(1) lookup instead of creating new maps
    val allMappings: Map<String, Set<String>> = imageMap + audioMap + videoMap

    // Using 'val' instead of 'get()' because these are static constants
    val imageMimeTypes = imageMap.keys
    val audioMimeTypes = audioMap.keys
    val videoMimeTypes = videoMap.keys

    val imageExtensions = imageMap.values.flatten().toSet()
    val audioExtensions = audioMap.values.flatten().toSet()
    val videoExtensions = videoMap.values.flatten().toSet()

    fun getMimeTypeFromExtension(ext: String): String? {
        val lowerExt = ext.lowercase()
        return allMappings.entries.firstOrNull { lowerExt in it.value }?.key
    }

    fun getExtensionFromMimeType(mimeType: String): String? =
        allMappings[mimeType]?.firstOrNull()
}

object AllowedMimeTypes {
    val ImageMimeTypes get() = FileRegistry.imageMimeTypes
    val AudioMimeTypes get() = FileRegistry.audioMimeTypes
    val VideoMimeTypes get() = FileRegistry.videoMimeTypes

    val All get() = ImageMimeTypes + AudioMimeTypes + VideoMimeTypes
}
object AllowedExtensions {
    val ImageExtensions get() = FileRegistry.imageExtensions
    val AudioExtensions get() = FileRegistry.audioExtensions
    val VideoExtensions get() = FileRegistry.videoExtensions

    val All get() = ImageExtensions + AudioExtensions + VideoExtensions
}

fun mediaKindForMimeType(mimeType: String): MediaKind = when(mimeType) {
    in AllowedMimeTypes.ImageMimeTypes -> MediaKind.IMAGE
    in AllowedMimeTypes.AudioMimeTypes -> MediaKind.AUDIO
    in AllowedMimeTypes.VideoMimeTypes -> MediaKind.VIDEO

    else -> error("Unsupported mime type: '$mimeType'")
}

fun mimeTypeToFileExtension(mimeType: String) = FileRegistry.getExtensionFromMimeType(mimeType)
package com.davanok.dvnkquizz.core.utils

import com.davanok.dvnkquizz.core.domain.enums.MediaKind


object AllowedMimeTypes {
    val ImageMimeTypes = setOf("")
    val AudioMimeTypes = setOf("")
    val VideoMimeTypes = setOf("")
}
object AllowedExtensions {
    val ImageExtensions = setOf("jpg", "jpeg", "png", "webp")
    val AudioExtensions = setOf("mp3")
    val VideoExtensions = setOf("mp4")
}

fun mediaKindForMimeType(mimeType: String): MediaKind = when(mimeType) {
    in AllowedMimeTypes.ImageMimeTypes -> MediaKind.IMAGE
    in AllowedMimeTypes.AudioMimeTypes -> MediaKind.AUDIO
    in AllowedMimeTypes.VideoMimeTypes -> MediaKind.VIDEO

    else -> error("Unsupported mime type: '$mimeType'")
}

fun mimeTypeToFileExtension(mimeType: String) = mimeType
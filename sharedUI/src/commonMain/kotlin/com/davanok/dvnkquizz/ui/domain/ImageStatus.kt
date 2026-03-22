package com.davanok.dvnkquizz.ui.domain

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.davanok.dvnkquizz.core.domain.entities.FileDownloadStatus

sealed interface ImageStatus {
    data class Loading(val percent: Float) : ImageStatus {
        init { check(percent in 0f..1f) }
    }
    data class Error(val throwable: Throwable) : ImageStatus
    data class Success(val bitmap: ImageBitmap) : ImageStatus
}

fun FileDownloadStatus.toImageStatus(bytes: ByteArray? = null): ImageStatus = when (this) {
    is FileDownloadStatus.ByteData -> ImageStatus.Success(data.decodeToImageBitmap())
    is FileDownloadStatus.Error -> ImageStatus.Error(throwable)
    is FileDownloadStatus.Loading -> ImageStatus.Loading((totalBytesReceived / contentLength).toFloat())
    FileDownloadStatus.Success -> ImageStatus.Success(bytes!!.decodeToImageBitmap())
}
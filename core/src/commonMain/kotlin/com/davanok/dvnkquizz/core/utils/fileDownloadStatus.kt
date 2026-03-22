package com.davanok.dvnkquizz.core.utils

import com.davanok.dvnkquizz.core.domain.entities.FileDownloadStatus
import io.github.jan.supabase.storage.DownloadStatus

internal fun DownloadStatus.toFileDownloadStatus(): FileDownloadStatus = when (this) {
    is DownloadStatus.ByteData -> FileDownloadStatus.ByteData(data)
    is DownloadStatus.Progress -> FileDownloadStatus.Loading(totalBytesReceived, contentLength)
    DownloadStatus.Success -> FileDownloadStatus.Success
}
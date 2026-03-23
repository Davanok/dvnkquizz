package com.davanok.dvnkquizz.core.domain.entities

sealed interface FileDownloadStatus {
    data class Loading(val totalBytesReceived: Long, val contentLength: Long) : FileDownloadStatus
    data class Error(val throwable: Throwable) : FileDownloadStatus
    data class ByteData(val data: ByteArray) : FileDownloadStatus {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ByteData

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()

        override fun toString(): String = "FileDownloadStatus.ByteData"
    }
    data object Success : FileDownloadStatus
}

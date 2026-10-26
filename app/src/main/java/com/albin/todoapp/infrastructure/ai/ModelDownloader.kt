package com.albin.todoapp.infrastructure.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long
) {
    val fraction: Float
        get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
}

class ModelDownloader @Inject constructor() {

    fun download(urlStr: String, targetFile: File, expectedSizeBytes: Long): Flow<DownloadProgress> = flow {
        val partFile = File(targetFile.parentFile, "${targetFile.name}.part")
        targetFile.parentFile?.mkdirs()

        val existingBytes = if (partFile.exists()) partFile.length() else 0L

        val url = URL(urlStr)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            if (existingBytes > 0) {
                setRequestProperty("Range", "bytes=$existingBytes-")
            }
        }

        connection.connect()
        val responseCode = connection.responseCode

        val effectiveConnection: HttpURLConnection
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
            val redirectUrl = connection.getHeaderField("Location")
            effectiveConnection = (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 30_000
                if (existingBytes > 0) {
                    setRequestProperty("Range", "bytes=$existingBytes-")
                }
            }
            effectiveConnection.connect()
        } else {
            effectiveConnection = connection
        }

        val code = effectiveConnection.responseCode
        if (code != 200 && code != 206) {
            partFile.delete()
            throw IllegalStateException(
                if (code == 401 || code == 403) {
                    "El modelo en Hugging Face requiere token de acceso. Continuarás en Modo Local."
                } else {
                    "Error al descargar modelo (HTTP $code). Continuarás en Modo Local."
                }
            )
        }

        val contentType = effectiveConnection.contentType
        if (contentType != null && (contentType.contains("text/html") || contentType.contains("application/json"))) {
            partFile.delete()
            throw IllegalStateException("El servidor no devolvió el archivo binario del modelo.")
        }

        val isResuming = code == 206
        var downloadedBytes = if (isResuming) existingBytes else 0L

        val contentLengthHeader = effectiveConnection.contentLengthLong
        val totalBytes = if (contentLengthHeader > 0) {
            if (isResuming) contentLengthHeader + existingBytes else contentLengthHeader
        } else {
            expectedSizeBytes
        }

        emit(DownloadProgress(downloadedBytes, totalBytes))

        val inputStream: InputStream = effectiveConnection.inputStream
        val outputStream = FileOutputStream(partFile, isResuming)

        val buffer = ByteArray(BUFFER_SIZE)
        var lastEmittedBytes = downloadedBytes

        try {
            outputStream.use { output ->
                inputStream.use { input ->
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (downloadedBytes - lastEmittedBytes >= EMIT_EVERY_BYTES) {
                            lastEmittedBytes = downloadedBytes
                            emit(DownloadProgress(downloadedBytes, totalBytes))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            partFile.delete()
            throw e
        }

        emit(DownloadProgress(downloadedBytes, totalBytes))

        if (targetFile.exists()) targetFile.delete()
        if (!partFile.renameTo(targetFile)) {
            partFile.copyTo(targetFile, overwrite = true)
            partFile.delete()
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val BUFFER_SIZE = 128 * 1024
        private const val EMIT_EVERY_BYTES = 2L * 1024 * 1024
    }
}

package com.skyworld.surveyapp.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Reads a file the user picked via the document picker into memory and wraps
 * it as a MultipartBody.Part ready to attach to the response submission.
 *
 * Files are capped at the API's 1MB-per-PDF limit; anything larger is
 * rejected client-side so the user gets immediate feedback instead of a
 * server error.
 */
object FileUtils {

    private const val MAX_FILE_SIZE_BYTES = 1L * 1024 * 1024 // 1MB, matches file_properties in the doc

    sealed class UriToPartResult {
        data class Success(val part: MultipartBody.Part) : UriToPartResult()
        data class TooLarge(val fileName: String) : UriToPartResult()
        data object ReadFailed : UriToPartResult()
    }

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String = "certificates"
    ): UriToPartResult {
        val resolver = context.contentResolver
        val fileName = queryFileName(resolver, uri) ?: "certificate.pdf"

        val bytes = try {
            resolver.openInputStream(uri)?.use { it.readBytes() } ?: return UriToPartResult.ReadFailed
        } catch (e: Exception) {
            return UriToPartResult.ReadFailed
        }

        if (bytes.size > MAX_FILE_SIZE_BYTES) {
            return UriToPartResult.TooLarge(fileName)
        }

        val requestBody = bytes.toRequestBody("application/pdf".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(partName, fileName, requestBody)
        return UriToPartResult.Success(part)
    }

    private fun queryFileName(resolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}

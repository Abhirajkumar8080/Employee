package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object StorageHelper {

    /**
     * Copies an external URI to app's internal storage so it remains persistently accessible.
     */
    fun persistImageUri(context: Context, uri: Uri, prefix: String): String? {
        return try {
            val docsDir = File(context.filesDir, "employee_docs")
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val destFile = File(docsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

package com.example.lessonmonitor.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Copies a picked photo (from the Android Photo Picker — no storage
 * permission required) into this app's private storage, since the source
 * `content://` URI's permission grant is not guaranteed to outlive the
 * picker call. Returns the absolute file path to store on
 * [com.example.lessonmonitor.data.local.entity.StudentEntity.photoPath], or
 * null if the copy failed.
 */
fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val directory = File(context.filesDir, "student_photos").apply { mkdirs() }
        val destination = File(directory, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        destination.absolutePath
    } catch (e: IOException) {
        null
    }
}

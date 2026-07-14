package com.example.lessonmonitor.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Writes [content] to a file under this app's private cache "exports"
 * subfolder (matches `res/xml/file_paths.xml`'s `cache-path`) and launches
 * the Android share sheet for it via a `FileProvider` `content://` Uri —
 * PLAN.md §6 tech decision ("shared via FileProvider + ACTION_SEND"), same
 * "app-private storage, no permission needed" spirit as [copyUriToInternalStorage].
 * Returns `true` on success, `false` if the file write failed.
 */
fun writeAndShareFile(context: Context, fileName: String, content: String, mimeType: String): Boolean {
    return try {
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(directory, fileName)
        file.writeText(content)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
        true
    } catch (e: IOException) {
        false
    }
}

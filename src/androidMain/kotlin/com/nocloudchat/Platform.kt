package com.nocloudchat

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun openFileInExplorer(path: String) {
    runCatching {
        val context = AppContextHolder.appContext
        val target = File(path)
        val directory = if (target.isDirectory) target else target.parentFile ?: target

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (directory.canonicalPath.startsWith(downloadDir.canonicalPath)) {
                val relative = directory.canonicalPath.removePrefix(downloadDir.canonicalPath).trimStart('/')
                data = DocumentsContract.buildRootUri(
                    "com.android.externalstorage.documents",
                    "home"
                )
                if (relative.isNotEmpty()) {
                    putExtra("android.provider.extra.INITIAL_URI", DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        "home:$relative"
                    ))
                }
            } else {
                data = DocumentsContract.buildRootUri(
                    "com.android.externalstorage.documents",
                    "primary"
                )
            }
            type = "vnd.android.document/root"
        }

        context.startActivity(intent)
    }
}

actual suspend fun pickFile(): File? {
    val launcher = AppContextHolder.filePickerLauncher ?: return null

    val deferred = CompletableDeferred<Uri?>()
    AppContextHolder.pendingFileDeferred = deferred

    withContext(Dispatchers.Main) {
        launcher("*/*")
    }

    val uri = deferred.await() ?: return null
    return copyUriToTempFile(AppContextHolder.appContext, uri)
}

private fun copyUriToTempFile(context: Context, uri: Uri): File? {
    return try {
        val fileName = getFileNameFromUri(context, uri) ?: "attachment"
        // Use a unique subdirectory per pick so the original filename is preserved
        // (file.name is used as the display name by the file-transfer layer).
        val tempDir = File(context.cacheDir, "ncc_${System.currentTimeMillis()}").also { it.mkdirs() }
        val tempFile = File(tempDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (_: Exception) {
        null
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

actual fun getDownloadDirectory(): File =
    File("/storage/emulated/0/Download/NoCloud Chat")

actual fun getPreferencesDirectory(): File =
    AppContextHolder.appContext.filesDir

@Suppress("DEPRECATION")
actual fun detectSsidPlatform(): String? = try {
    val context = AppContextHolder.appContext
    val raw: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // API 29+: read WifiInfo from active network capabilities
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return null)
        val wifiInfo = caps?.transportInfo as? android.net.wifi.WifiInfo
        wifiInfo?.ssid
    } else {
        // API 26-28: legacy WifiManager path
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wm.connectionInfo?.ssid
    }
    // Strip surrounding quotes Android sometimes adds, reject placeholder values
    raw?.trim('"')?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
} catch (_: Exception) {
    null
}

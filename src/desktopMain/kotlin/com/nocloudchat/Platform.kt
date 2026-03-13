package com.nocloudchat

import com.nocloudchat.network.detectSsid
import java.io.File

actual fun openFileInExplorer(path: String) {
    try { java.awt.Desktop.getDesktop().open(File(path).parentFile) } catch (_: Exception) {}
}

actual fun getDownloadDirectory(): File =
    File(System.getProperty("user.home"), "Downloads/NoCloud Chat")

actual fun detectSsidPlatform(): String? = detectSsid()

actual fun getPreferencesDirectory(): File =
    File(System.getProperty("user.home"), ".nocloudchat")

actual suspend fun pickFile(): File? {
    var result: File? = null
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select File", java.awt.FileDialog.LOAD)
    dialog.isVisible = true
    val dir = dialog.directory
    val file = dialog.file
    if (dir != null && file != null) {
        result = File(dir, file)
    }
    return result
}

package com.nocloudchat

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import java.io.File

actual fun openFileInExplorer(path: String) {
    // TODO: launch Android file manager Intent
}

actual fun pickFile(): java.io.File? = null // TODO: launch Android file picker Intent

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

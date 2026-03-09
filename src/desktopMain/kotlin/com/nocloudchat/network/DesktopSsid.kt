package com.nocloudchat.network

/**
 * Tries to read the connected WiFi SSID using platform-specific CLI tools.
 * Returns null if not on WiFi, the command fails, or the SSID is blank.
 * Desktop-only — uses ProcessBuilder to invoke OS commands.
 */
fun detectSsid(): String? {
    val os = System.getProperty("os.name", "").lowercase()
    return try {
        when {
            os.contains("win") -> {
                val output = runNetworkCommand("netsh", "wlan", "show", "interfaces") ?: return null
                Regex("""^\s+SSID\s+:\s+(.+)$""", RegexOption.MULTILINE)
                    .find(output)?.groupValues?.getOrNull(1)?.trim()
            }
            os.contains("mac") -> {
                val airport = "/System/Library/PrivateFrameworks/Apple80211.framework/" +
                              "Versions/Current/Resources/airport"
                val output = runNetworkCommand(airport, "-I") ?: return null
                Regex("""^\s+SSID:\s+(.+)$""", RegexOption.MULTILINE)
                    .find(output)?.groupValues?.getOrNull(1)?.trim()
            }
            else -> {
                // Try iwgetid first, fall back to nmcli
                val iwgetid = runNetworkCommand("iwgetid", "-r")?.trim()?.ifBlank { null }
                if (iwgetid != null) return iwgetid
                val nmcli = runNetworkCommand(
                    "nmcli", "-t", "-f", "active,ssid", "dev", "wifi"
                ) ?: return null
                nmcli.lines().firstOrNull { it.startsWith("yes:") }
                    ?.removePrefix("yes:")?.trim()?.ifBlank { null }
            }
        }?.ifBlank { null }
    } catch (_: Exception) { null }
}

private fun runNetworkCommand(vararg args: String): String? = try {
    val process = ProcessBuilder(*args)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
    output
} catch (_: Exception) { null }

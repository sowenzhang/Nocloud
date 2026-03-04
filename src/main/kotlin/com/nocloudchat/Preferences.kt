package com.nocloudchat

import org.json.JSONObject
import java.io.File

/**
 * Persists user settings to ~/.nocloudchat/settings.json.
 * All reads/writes are synchronous and should be called off the main thread.
 */
object Preferences {
    private val dir = File(System.getProperty("user.home"), ".nocloudchat")
    private val file = File(dir, "settings.json")

    private fun load(): JSONObject = try {
        if (file.exists()) JSONObject(file.readText()) else JSONObject()
    } catch (_: Exception) {
        JSONObject()
    }

    private fun save(json: JSONObject) {
        dir.mkdirs()
        file.writeText(json.toString(2))
    }

    var displayName: String?
        get() = load().optString("displayName", "").takeIf { it.isNotBlank() }
        set(value) {
            val json = load()
            if (value != null) json.put("displayName", value) else json.remove("displayName")
            save(json)
        }

    var isDarkMode: Boolean?
        get() = load().let { if (it.has("isDarkMode")) it.getBoolean("isDarkMode") else null }
        set(value) {
            val json = load()
            if (value != null) json.put("isDarkMode", value) else json.remove("isDarkMode")
            save(json)
        }

    var trustedNetworks: Set<String>
        get() {
            val arr = load().optJSONArray("trustedNetworks") ?: return emptySet()
            return (0 until arr.length()).map { arr.getString(it) }.toSet()
        }
        set(value) {
            val json = load()
            json.put("trustedNetworks", org.json.JSONArray(value.toList()))
            save(json)
        }
}


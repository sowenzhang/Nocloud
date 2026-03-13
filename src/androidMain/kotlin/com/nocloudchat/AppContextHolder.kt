package com.nocloudchat

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred

/**
 * Holds the application Context so platform functions (expect/actual) can access
 * Android APIs without threading it through every call site.
 * Set once in MainActivity.onCreate() before AppState is created.
 */
object AppContextHolder {
    lateinit var appContext: Context

    /** Set by MainActivity to launch the system file picker. */
    var filePickerLauncher: ((String) -> Unit)? = null

    /** Completed by the ActivityResultLauncher callback with the chosen URI (or null on cancel). */
    var pendingFileDeferred: CompletableDeferred<Uri?>? = null
}

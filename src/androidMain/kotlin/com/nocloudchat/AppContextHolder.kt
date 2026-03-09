package com.nocloudchat

import android.content.Context

/**
 * Holds the application Context so platform functions (expect/actual) can access
 * Android APIs without threading it through every call site.
 * Set once in MainActivity.onCreate() before AppState is created.
 */
object AppContextHolder {
    lateinit var appContext: Context
}

package com.nocloudchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nocloudchat.state.AppState

class MainActivity : ComponentActivity() {
    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge so the app draws behind the status bar and
        // navigation bar. Compose handles window insets (IME + safe areas) via
        // Modifier.safeDrawingPadding() and Modifier.imePadding() in App.kt.
        enableEdgeToEdge()
        // Context must be set before AppState, which reads Preferences on init.
        AppContextHolder.appContext = applicationContext
        appState = AppState()
        setContent {
            App(appState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appState.isInitialized) appState.shutdown()
    }
}

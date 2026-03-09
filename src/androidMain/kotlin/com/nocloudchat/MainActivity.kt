package com.nocloudchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nocloudchat.state.AppState

class MainActivity : ComponentActivity() {
    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

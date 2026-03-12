package com.nocloudchat

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nocloudchat.state.AppState

class MainActivity : ComponentActivity() {
    private lateinit var appState: AppState

    // Multicast lock: required so Android's WiFi driver delivers UDP broadcast
    // packets to our Discovery socket. Without it, the OS silently drops all
    // incoming broadcast datagrams, making peer discovery unreliable.
    private var multicastLock: WifiManager.MulticastLock? = null

    // WiFi lock: prevents the WiFi radio from sleeping while the app is in the
    // foreground, keeping discovery broadcasts and TCP messaging stable.
    private var wifiLock: WifiManager.WifiLock? = null

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

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        multicastLock = wifiManager?.createMulticastLock("NoCloudChatMulticast")
        wifiLock = wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "NoCloudChatWifi")
    }

    override fun onResume() {
        super.onResume()
        if (multicastLock?.isHeld == false) multicastLock?.acquire()
        if (wifiLock?.isHeld == false) wifiLock?.acquire()
    }

    override fun onPause() {
        super.onPause()
        if (multicastLock?.isHeld == true) multicastLock?.release()
        if (wifiLock?.isHeld == true) wifiLock?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appState.isInitialized) appState.shutdown()
    }
}

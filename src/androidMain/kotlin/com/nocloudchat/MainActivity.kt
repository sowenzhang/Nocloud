package com.nocloudchat

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

    // File picker launcher — registered here so it is tied to the Activity lifecycle.
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        AppContextHolder.pendingFileDeferred?.complete(uri)
        AppContextHolder.pendingFileDeferred = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge so the app draws behind the status bar and
        // navigation bar. Compose handles window insets (IME + safe areas) via
        // Modifier.safeDrawingPadding() and Modifier.imePadding() in App.kt.
        enableEdgeToEdge()
        // Context must be set before AppState, which reads Preferences on init.
        AppContextHolder.appContext = applicationContext
        // Expose the launcher so pickFile() (called from platform code) can open the picker.
        AppContextHolder.filePickerLauncher = { mimeType -> filePickerLauncher.launch(mimeType) }
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
        AppContextHolder.filePickerLauncher = null
        AppContextHolder.pendingFileDeferred?.complete(null)
        AppContextHolder.pendingFileDeferred = null
        if (::appState.isInitialized) appState.shutdown()
    }
}

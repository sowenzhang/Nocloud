package com.nocloudchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nocloudchat.state.AppState
import com.nocloudchat.ui.*
import com.nocloudchat.ui.theme.AppTheme
import com.nocloudchat.ui.theme.NoCloudChatColors

@Composable
fun App(state: AppState) {
    val isDark by state.isDarkMode.collectAsState()
    AppTheme(isDark = isDark) {
        var showSettings by remember { mutableStateOf(false) }
        var showAbout    by remember { mutableStateOf(false) }
        val activePeerId by state.activePeerId.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NoCloudChatColors.Background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // ── Sidebar ───────────────────────────────────────────────────
                Sidebar(
                    state          = state,
                    onOpenSettings = { showSettings = true },
                    onOpenAbout    = { showAbout = true },
                )

                // Vertical divider
                HorizontalDivider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color    = NoCloudChatColors.Border,
                )

                // ── Main content ──────────────────────────────────────────────
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (activePeerId == null) {
                        WelcomePanel()
                    } else {
                        ChatPanel(state = state, peerId = activePeerId!!)
                    }
                }
            }

            // ── Toast overlay ─────────────────────────────────────────────────
            ToastHost(state)
        }

        // ── Settings dialog ───────────────────────────────────────────────────
        if (showSettings) {
            SettingsDialog(
                state       = state,
                onDismiss   = { showSettings = false },
                onOpenAbout = { showSettings = false; showAbout = true },
            )
        }

        // ── About dialog ──────────────────────────────────────────────────────
        if (showAbout) {
            AboutDialog(onDismiss = { showAbout = false })
        }
    }
}


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

// Breakpoint below which the app uses a single-pane mobile layout
private val MOBILE_BREAKPOINT = 600.dp

@Composable
fun App(state: AppState) {
    val isDark by state.isDarkMode.collectAsState()
    AppTheme(isDark = isDark) {
        var showSettings         by remember { mutableStateOf(false) }
        var showAbout            by remember { mutableStateOf(false) }
        var showSecretJoinDialog by remember { mutableStateOf(false) }
        var showTutorial         by remember { mutableStateOf(!Preferences.hasSeenTutorial) }
        val activePeerId         by state.activePeerId.collectAsState()

        LaunchedEffect(state) {
            state.secretJoinRequest.collect {
                showSecretJoinDialog = true
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(NoCloudChatColors.Background)
                .safeDrawingPadding()
                .imePadding()
        ) {
            val isMobile = maxWidth < MOBILE_BREAKPOINT

            if (isMobile) {
                // ── Mobile: single-pane, navigate between contacts and chat ──
                MobileLayout(
                    state          = state,
                    activePeerId   = activePeerId,
                    onOpenSettings = { showSettings = true },
                    onOpenAbout    = { showAbout = true },
                )
            } else {
                // ── Desktop: side-by-side sidebar + chat ─────────────────────
                Row(modifier = Modifier.fillMaxSize()) {
                    Sidebar(
                        state          = state,
                        onOpenSettings = { showSettings = true },
                        onOpenAbout    = { showAbout = true },
                        modifier       = Modifier.width(280.dp),
                    )

                    HorizontalDivider(
                        modifier = Modifier.fillMaxHeight().width(1.dp),
                        color    = NoCloudChatColors.Border,
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (activePeerId == null) {
                            WelcomePanel()
                        } else {
                            ChatPanel(state = state, peerId = activePeerId!!)
                        }
                    }
                }
            }

            // ── Toast overlay (always on top) ─────────────────────────────────
            ToastHost(state)

            // ── First-run tutorial ────────────────────────────────────────────
            if (showTutorial) {
                TutorialOverlay(
                    onDismiss = {
                        Preferences.hasSeenTutorial = true
                        showTutorial = false
                    }
                )
            }
        }

        // ── Dialogs ───────────────────────────────────────────────────────────
        if (showSettings) {
            SettingsDialog(
                state           = state,
                onDismiss       = { showSettings = false },
                onOpenAbout     = { showSettings = false; showAbout = true },
                onShowTutorial  = { showTutorial = true },
            )
        }

        if (showAbout) {
            AboutDialog(onDismiss = { showAbout = false })
        }

        if (showSecretJoinDialog) {
            SecretJoinDialog(
                onDismiss = { showSecretJoinDialog = false },
                onJoin = { passphrase ->
                    state.setNetworkSecretEnabled(true)
                    state.setNetworkSecret(passphrase)
                    showSecretJoinDialog = false
                },
            )
        }
    }
}

@Composable
private fun MobileLayout(
    state: AppState,
    activePeerId: String?,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    // On mobile, show chat panel when a peer is active, contacts list otherwise.
    // The back arrow in the chat header calls state.closeChat() to return here.
    if (activePeerId != null) {
        ChatPanel(
            state  = state,
            peerId = activePeerId,
            onBack = { state.closeChat() },
        )
    } else {
        Sidebar(
            state          = state,
            onOpenSettings = onOpenSettings,
            onOpenAbout    = onOpenAbout,
            modifier       = Modifier.fillMaxWidth(),
        )
    }
}

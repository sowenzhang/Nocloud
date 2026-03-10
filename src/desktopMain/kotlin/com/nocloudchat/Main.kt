package com.nocloudchat

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.nocloudchat.state.AppState
import java.awt.Dimension
import java.awt.Taskbar
import javax.imageio.ImageIO

fun main() {
    val appState = AppState()
    val appIcon = runCatching {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")!!
        BitmapPainter(ImageIO.read(stream).toComposeImageBitmap())
    }.getOrNull()

    application {
        val windowState = rememberWindowState(size = DpSize(980.dp, 700.dp))
        val trayState = rememberTrayState()

        val unreadCounts by appState.unreadCounts.collectAsState()
        val totalUnread = unreadCounts.values.sum()
        val title = if (totalUnread > 0) "($totalUnread) NoCloud Chat 🏠" else "NoCloud Chat 🏠"

        // Send a tray notification whenever a message arrives while the window is minimized
        LaunchedEffect(Unit) {
            appState.toastEvents.collect { event ->
                if (windowState.isMinimized) {
                    trayState.sendNotification(
                        Notification(
                            title   = event.senderName,
                            message = event.text,
                            type    = Notification.Type.Info,
                        )
                    )
                }
            }
        }

        // Update the OS-level icon badge (macOS Dock badge, Windows 11 taskbar badge)
        LaunchedEffect(totalUnread) {
            runCatching {
                val taskbar = Taskbar.getTaskbar()
                if (taskbar.isSupported(Taskbar.Feature.ICON_BADGE_TEXT)) {
                    taskbar.setIconBadge(if (totalUnread > 0) totalUnread.toString() else null)
                }
            }
        }

        // System tray icon — shows unread count in tooltip, lets user restore the window
        if (appIcon != null) {
            Tray(
                icon    = appIcon,
                state   = trayState,
                tooltip = if (totalUnread > 0) "NoCloud Chat — $totalUnread unread" else "NoCloud Chat",
                menu = {
                    Item("Open NoCloud Chat") { windowState.isMinimized = false }
                    Separator()
                    Item("Quit") { appState.shutdown(); exitApplication() }
                }
            )
        }

        Window(
            onCloseRequest = {
                appState.shutdown()
                exitApplication()
            },
            state     = windowState,
            title     = title,
            icon      = appIcon,
            resizable = true,
        ) {
            window.minimumSize = Dimension(600, 480)
            App(appState)
        }
    }
}

package com.nocloudchat

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import javax.imageio.ImageIO
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.nocloudchat.state.AppState
import com.nocloudchat.ui.theme.NoCloudChatColors
import java.awt.Dimension

fun main() {
    val appState = AppState()
    val appIcon = runCatching {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")!!
        BitmapPainter(ImageIO.read(stream).toComposeImageBitmap())
    }.getOrNull()

    application {
        val windowState = rememberWindowState(
            size = DpSize(980.dp, 700.dp),
        )

        Window(
            onCloseRequest = {
                appState.shutdown()
                exitApplication()
            },
            state    = windowState,
            title    = "NoCloud Chat 🏠",
            icon     = appIcon,
            resizable = true,
        ) {
            // Set minimum window size
            window.minimumSize = Dimension(600, 480)

            App(appState)
        }
    }
}

package com.nocloudchat.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.ui.theme.NoCloudChatColors

private data class TutorialStep(
    val emoji: String,
    val title: String,
    val body: String,
)

private val steps = listOf(
    TutorialStep(
        emoji = "👋",
        title = "Welcome to NoCloud Chat",
        body  = "Chat with anyone on your Wi-Fi — no internet, no accounts, no cloud. Everything stays on your local network.",
    ),
    TutorialStep(
        emoji = "📡",
        title = "Automatic discovery",
        body  = "The app finds everyone running NoCloud Chat on the same Wi-Fi or router. Just launch the app and people appear in your contacts list.",
    ),
    TutorialStep(
        emoji = "🔒",
        title = "Private by design",
        body  = "Messages never leave your network and are never stored on any server. Optionally add a network passphrase in Settings to keep strangers out.",
    ),
    TutorialStep(
        emoji = "💬",
        title = "Start chatting",
        body  = "Tap a contact to open a private chat. You can also send files and voice messages — no size limits, no throttling.",
    ),
)

@Composable
fun TutorialOverlay(onDismiss: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    val step = steps[currentStep]
    val isLast = currentStep == steps.lastIndex

    // Dim backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        // Card
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NoCloudChatColors.Surface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Emoji illustration
            Text(step.emoji, fontSize = 56.sp)

            Spacer(Modifier.height(20.dp))

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == currentStep) 20.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (i == currentStep) NoCloudChatColors.Accent
                                else NoCloudChatColors.Border
                            )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Fixed-height content area keeps the card the same size on every step
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Title
                    Text(
                        text = step.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoCloudChatColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))

                    // Body
                    Text(
                        text = step.body,
                        fontSize = 14.sp,
                        color = NoCloudChatColors.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Primary action
            Button(
                onClick = {
                    if (isLast) onDismiss() else currentStep++
                },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NoCloudChatColors.Accent),
            ) {
                Text(
                    text = if (isLast) "Get started" else "Next",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            // Skip link (only before last step)
            if (!isLast) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Skip", fontSize = 13.sp, color = NoCloudChatColors.TextDim)
                }
            }
        }
    }
}

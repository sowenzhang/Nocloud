package com.nocloudchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nocloudchat.ui.theme.NoCloudChatColors

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NoCloudChatColors.Surface)
                .width(420.dp)
                .heightIn(max = 580.dp),   // never taller than this
        ) {

            // ── Sticky header (always visible) ────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
            ) {
                Text("🏠", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "NoCloud Chat",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NoCloudChatColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                // X close button — always visible
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(NoCloudChatColors.Background)
                        .clickable { onDismiss() }
                ) {
                    Text("✕", fontSize = 13.sp, color = NoCloudChatColors.TextMuted)
                }
            }
            HorizontalDivider(color = NoCloudChatColors.Border)

            // ── Scrollable content ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Version 1.0.0",
                    fontSize = 11.sp,
                    color = NoCloudChatColors.TextDim,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Chat with your family at home — no internet, no accounts, no drama.",
                    fontSize = 13.sp,
                    color = NoCloudChatColors.TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                )

                Spacer(Modifier.height(16.dp))

                PrivacyCard(
                    icon  = "📡",
                    title = "Stays right inside your home",
                    body  = "Messages travel directly between devices on your Wi-Fi. " +
                            "They never leave your router — not even for a split second.",
                )
                Spacer(Modifier.height(8.dp))
                PrivacyCard(
                    icon  = "🚫",
                    title = "No internet connection — ever",
                    body  = "NoCloud Chat never connects to any server on the internet. " +
                            "There is no cloud, no company server. Your messages belong to you.",
                )
                Spacer(Modifier.height(8.dp))
                PrivacyCard(
                    icon  = "👤",
                    title = "No accounts or sign-ups",
                    body  = "Just pick a name and start chatting. No email, no password, " +
                            "nothing to create, nothing to lose.",
                )
                Spacer(Modifier.height(8.dp))
                PrivacyCard(
                    icon  = "🔒",
                    title = "Only your home can see you",
                    body  = "The app discovers only people on the same Wi-Fi router. " +
                            "Neighbours, coffee shops, and the internet cannot find you.",
                )
                Spacer(Modifier.height(8.dp))
                PrivacyCard(
                    icon  = "🗑️",
                    title = "Nothing is stored anywhere",
                    body  = "Messages live only in memory while the app is open. " +
                            "Close the app and they're gone — no logs, no databases.",
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    "Made with ❤️ for families who believe\na home chat should stay in the home.",
                    fontSize = 12.sp,
                    color = NoCloudChatColors.TextDim,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }

            // ── Sticky footer (always visible) ────────────────────────────────
            HorizontalDivider(color = NoCloudChatColors.Border)
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoCloudChatColors.Accent,
                        contentColor   = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Got it!", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PrivacyCard(icon: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NoCloudChatColors.Surface2.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(icon, fontSize = 20.sp, modifier = Modifier.padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = NoCloudChatColors.TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(body, fontSize = 11.sp, color = NoCloudChatColors.TextMuted, lineHeight = 16.sp)
        }
    }
}

package com.nocloudchat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.ui.theme.NoCloudChatColors

@Composable
fun WelcomePanel() {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("💬", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Select a peer to chat",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = NoCloudChatColors.TextDim,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Click anyone in the sidebar to start an instant,\nprivate conversation — no accounts needed.",
            fontSize = 13.sp,
            color = NoCloudChatColors.TextMuted,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

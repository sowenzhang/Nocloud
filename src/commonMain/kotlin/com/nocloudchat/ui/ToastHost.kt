package com.nocloudchat.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.state.AppState
import com.nocloudchat.ui.theme.NoCloudChatColors
import kotlinx.coroutines.delay

@Composable
fun ToastHost(state: AppState) {
    var current by remember { mutableStateOf<AppState.ToastEvent?>(null) }

    LaunchedEffect(Unit) {
        state.toastEvents.collect { event ->
            current = event
            delay(4_000)
            current = null
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        ) {
            current?.let { toast ->
                Box(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NoCloudChatColors.Surface2)
                        .border(1.dp, NoCloudChatColors.Border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = toast.senderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NoCloudChatColors.Accent2,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = toast.text,
                            fontSize = 13.sp,
                            color = NoCloudChatColors.TextPrimary,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

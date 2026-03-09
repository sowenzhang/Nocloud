package com.nocloudchat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.model.Message
import com.nocloudchat.model.MessageType
import com.nocloudchat.pickFile
import com.nocloudchat.state.AppState
import com.nocloudchat.state.FileTransferState
import com.nocloudchat.state.formatFileSize
import com.nocloudchat.ui.components.Avatar
import com.nocloudchat.ui.theme.NoCloudChatColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatPanel(state: AppState, peerId: String, onBack: (() -> Unit)? = null) {
    val peers by state.peers.collectAsState()
    val allMessages by state.messages.collectAsState()
    val fileTransfers by state.fileTransfers.collectAsState()
    val peer = peers.find { it.id == peerId }
    val messages = allMessages[peerId] ?: emptyList()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Focus input when chat opens
    LaunchedEffect(peerId) {
        focusRequester.requestFocus()
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isBlank() || isSending) return
        isSending = true
        inputText = ""
        scope.launch {
            try {
                state.sendMessage(peerId, text)
            } catch (e: Exception) {
                inputText = text  // restore on failure
            } finally {
                isSending = false
            }
        }
    }

    fun pickAndSendFile() {
        scope.launch(Dispatchers.IO) {
            val selectedFile = pickFile()
            selectedFile?.let {
                try { state.sendFile(peerId, it) } catch (_: Exception) {}
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header ───────────────────────────────────────────────────────────
        ChatHeader(peerName = peer?.name ?: peerId, onBack = onBack)
        HorizontalDivider(color = NoCloudChatColors.Border, thickness = 1.dp)

        // ── Message list ──────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (messages.isNotEmpty()) {
                    item { DateDivider(messages.first().timestamp) }
                }
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        msg = msg,
                        myId = state.myId,
                        fileTransfers = fileTransfers,
                        onAcceptFile = { state.acceptFileOffer(it) },
                        onOpenFolder = { state.openLocalFile(it) },
                    )
                }
            }

            // Top fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NoCloudChatColors.Background, Color.Transparent)
                        )
                    )
                    .align(Alignment.TopCenter)
            )
        }

        HorizontalDivider(color = NoCloudChatColors.Border, thickness = 1.dp)

        // ── Input bar ─────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(NoCloudChatColors.Surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Type a message… (Alt+Enter to send)",
                        color = NoCloudChatColors.TextDim,
                        fontSize = 14.sp,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown &&
                            event.key == Key.Enter &&
                            event.isAltPressed
                        ) {
                            sendMessage()
                            true
                        } else false
                    },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoCloudChatColors.Accent,
                    unfocusedBorderColor = NoCloudChatColors.Border,
                    focusedTextColor = NoCloudChatColors.TextPrimary,
                    unfocusedTextColor = NoCloudChatColors.TextPrimary,
                    cursorColor = NoCloudChatColors.Accent,
                    focusedContainerColor = NoCloudChatColors.Background,
                    unfocusedContainerColor = NoCloudChatColors.Background,
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                maxLines = 4,
                singleLine = false,
            )

            Spacer(Modifier.width(8.dp))

            // Attach file button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.dp, NoCloudChatColors.Border, CircleShape)
                    .background(NoCloudChatColors.Background)
                    .clickable { pickAndSendFile() }
            ) {
                Text("📎", fontSize = 18.sp)
            }

            Spacer(Modifier.width(8.dp))

            // Send button
            val canSend = inputText.isNotBlank() && !isSending
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canSend) NoCloudChatColors.Accent else NoCloudChatColors.TextDim)
                    .clickable(enabled = canSend) { sendMessage() }
            ) {
                Text("➤", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun ChatHeader(peerName: String, onBack: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(NoCloudChatColors.Surface)
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        if (onBack != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
            ) {
                Text("‹", fontSize = 26.sp, color = NoCloudChatColors.TextPrimary, fontWeight = FontWeight.Light)
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Avatar(name = peerName, id = peerName)
        Spacer(Modifier.width(12.dp))
        Text(
            text = peerName,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NoCloudChatColors.TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NoCloudChatColors.OnlineGreen)
            )
            Spacer(Modifier.width(5.dp))
            Text("Online", fontSize = 12.sp, color = NoCloudChatColors.OnlineGreen)
        }
    }
}

@Composable
private fun MessageBubble(
    msg: Message,
    myId: String,
    fileTransfers: Map<String, FileTransferState>,
    onAcceptFile: (Message) -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    val isOut = msg.fromId == myId

    Column(
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (!isOut) {
            Text(
                text = msg.fromName,
                fontSize = 11.sp,
                color = NoCloudChatColors.TextMuted,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }

        when (msg.type) {
            MessageType.TEXT -> TextBubble(msg = msg, isOut = isOut)
            MessageType.FILE_OFFER -> FileBubble(
                msg = msg,
                isOut = isOut,
                transfer = fileTransfers[msg.id],
                onAccept = { onAcceptFile(msg) },
                onOpenFolder = onOpenFolder,
            )
            MessageType.HELLO -> { /* handshake only — never rendered in chat */ }
        }

        Text(
            text = formatTime(msg.timestamp),
            fontSize = 10.sp,
            color = NoCloudChatColors.TextDim,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp, end = 4.dp),
        )
    }
}

@Composable
private fun TextBubble(msg: Message, isOut: Boolean) {
    val colors = NoCloudChatColors
    val bubbleBg = if (isOut) colors.MessageOut else colors.MessageIn
    val textColor = if (isOut) colors.MessageOutText else colors.MessageInText
    val shape = if (isOut) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }
    Box(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .clip(shape)
            .background(bubbleBg)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = msg.text,
            fontSize = 14.sp,
            color = textColor,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun FileBubble(
    msg: Message,
    isOut: Boolean,
    transfer: FileTransferState?,
    onAccept: () -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    val colors = NoCloudChatColors
    val bubbleBg   = if (isOut) colors.MessageOut      else colors.MessageIn
    val textColor  = if (isOut) colors.MessageOutText  else colors.MessageInText
    val mutedColor = if (isOut) colors.MessageOutMuted else colors.MessageInMuted
    val shape = if (isOut) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Box(
        modifier = Modifier
            .widthIn(min = 220.dp, max = 340.dp)
            .clip(shape)
            .background(bubbleBg)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // File icon + name + size
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📎", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = msg.fileName ?: "file",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatFileSize(msg.fileSize ?: 0L),
                        fontSize = 11.sp,
                        color = mutedColor,
                    )
                }
            }

            // Action / status area
            when {
                // Incoming offer not yet accepted
                transfer == null && !isOut -> {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.Accent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                // Transfer in progress
                transfer != null && (
                    transfer.status == FileTransferState.Status.SENDING ||
                    transfer.status == FileTransferState.Status.RECEIVING
                ) -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { transfer.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isOut) Color.White else colors.Accent,
                            trackColor = Color.White.copy(alpha = 0.25f),
                        )
                        Text(
                            text = "${formatFileSize(transfer.bytesTransferred)} / ${formatFileSize(transfer.fileSize)}",
                            fontSize = 10.sp,
                            color = mutedColor,
                        )
                    }
                }

                // Complete
                transfer?.status == FileTransferState.Status.COMPLETE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "✓ ${if (isOut) "Sent" else "Saved"}",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!isOut && transfer.localPath != null) {
                            TextButton(
                                onClick = { onOpenFolder(transfer.localPath) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp),
                            ) {
                                Text("Open folder", fontSize = 11.sp, color = colors.Accent)
                            }
                        }
                    }
                }

                // Failed
                transfer?.status == FileTransferState.Status.FAILED -> {
                    Text(text = "✗ Transfer failed", fontSize = 12.sp, color = textColor)
                }

                // Outgoing — waiting for receiver to connect
                else -> {
                    Text(text = "Waiting for receiver…", fontSize = 12.sp, color = mutedColor)
                }
            }
        }
    }
}

@Composable
private fun DateDivider(timestamp: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = NoCloudChatColors.Border)
        Spacer(Modifier.width(12.dp))
        Text(
            text = formatDate(timestamp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = NoCloudChatColors.TextDim,
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = NoCloudChatColors.Border)
    }
}

private fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

private fun formatDate(ts: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = ts }
    val today = Calendar.getInstance()
    return if (
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    ) "Today"
    else SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(ts))
}

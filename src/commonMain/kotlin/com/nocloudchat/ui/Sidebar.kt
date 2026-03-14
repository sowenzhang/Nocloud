package com.nocloudchat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocloudchat.network.formatNetworkDisplay
import com.nocloudchat.state.AppState
import com.nocloudchat.ui.components.Avatar
import com.nocloudchat.ui.theme.NoCloudChatColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Sidebar(
    state: AppState,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myName by state.myName.collectAsState()
    val peers by state.peers.collectAsState()
    val protectedPeers by state.protectedPeers.collectAsState()
    val activePeerId by state.activePeerId.collectAsState()
    val unread by state.unreadCounts.collectAsState()
    val isPrivateNetwork by state.isPrivateNetwork.collectAsState()
    val networkTrusted   by state.networkTrusted.collectAsState()
    val currentNetworkId by state.currentNetworkId.collectAsState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(NoCloudChatColors.Surface)
    ) {
        // ── My info ──────────────────────────────────────────────────────────
        MyInfoBar(name = myName, id = state.myId, onOpenSettings = onOpenSettings)

        HorizontalDivider(color = NoCloudChatColors.Border, thickness = 1.dp)

        // ── Section label ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "ON YOUR NETWORK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = NoCloudChatColors.TextMuted,
            )
            if (peers.isNotEmpty() || protectedPeers.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NoCloudChatColors.Accent)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = (peers.size + protectedPeers.size).toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        // ── Peer list ─────────────────────────────────────────────────────────
        if (peers.isEmpty() && protectedPeers.isEmpty()) {
            NoPeersState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                items(peers, key = { it.id }) { peer ->
                    PeerItem(
                        peerName = peer.name,
                        peerId = peer.id,
                        onlineSince = peer.onlineSince,
                        isActive = peer.id == activePeerId,
                        unreadCount = unread[peer.id] ?: 0,
                        onClick = { state.openChat(peer.id) },
                    )
                }
                items(protectedPeers, key = { "protected_${it.id}" }) { peer ->
                    PeerItem(
                        peerName = peer.name,
                        peerId = peer.id,
                        onlineSince = peer.onlineSince,
                        isActive = false,
                        unreadCount = 0,
                        hasPassphrase = true,
                        peerIp = peer.ip,
                        onClick = { state.requestJoinProtectedPeer() },
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        // ── Network trust + about ─────────────────────────────────────────────
        HorizontalDivider(color = NoCloudChatColors.Border, thickness = 1.dp)
        NetworkStatusBar(
            isPrivateNetwork = isPrivateNetwork,
            networkTrusted   = networkTrusted,
            currentNetworkId = currentNetworkId,
            onTrustNetwork   = { state.trustCurrentNetwork() },
            onOpenAbout      = onOpenAbout,
        )
    }
}

@Composable
private fun MyInfoBar(name: String, id: String, onOpenSettings: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) Color.White.copy(alpha = 0.04f) else Color.Transparent,
        label = "myInfoBg"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .hoverable(interactionSource)
            .clickable { onOpenSettings() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Avatar(name = name, id = id, isSelf = true)
        Spacer(Modifier.width(10.dp))
        Text(
            text = name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = NoCloudChatColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text("⚙", fontSize = 16.sp, color = NoCloudChatColors.TextMuted)
    }
}

@Composable
private fun PeerItem(
    peerName: String,
    peerId: String,
    onlineSince: Long,
    isActive: Boolean,
    unreadCount: Int,
    hasPassphrase: Boolean = false,
    peerIp: String? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            isActive -> NoCloudChatColors.Accent.copy(alpha = 0.15f)
            hovered  -> Color.White.copy(alpha = 0.05f)
            else     -> Color.Transparent
        },
        label = "peerBg"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .hoverable(interactionSource)
            .clickable { onClick() }
            .padding(10.dp),
    ) {
        // Avatar with status indicator overlay
        Box {
            Avatar(name = if (hasPassphrase) "" else peerName, id = peerId)
            if (hasPassphrase) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(NoCloudChatColors.Surface)
                        .align(Alignment.BottomEnd)
                ) {
                    Text("🔒", fontSize = 9.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(NoCloudChatColors.Surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(NoCloudChatColors.OnlineGreen)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasPassphrase) "Protected peer" else peerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (hasPassphrase) NoCloudChatColors.TextMuted else NoCloudChatColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    hasPassphrase && peerIp != null -> "Passphrase required · $peerIp"
                    hasPassphrase -> "Passphrase required · tap to connect"
                    else -> "Online since ${formatSidebarTime(onlineSince)}"
                },
                fontSize = 11.sp,
                color = NoCloudChatColors.TextMuted,
                maxLines = 1,
            )
        }

        if (unreadCount > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NoCloudChatColors.Accent)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = unreadCount.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun NoPeersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔍", fontSize = 32.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No peers found yet.",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NoCloudChatColors.TextMuted,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Make sure NoCloud Chat is running on another device on the same Wi-Fi or router.",
            fontSize = 12.sp,
            color = NoCloudChatColors.TextDim,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun NetworkStatusBar(
    isPrivateNetwork: Boolean,
    networkTrusted: Boolean,
    currentNetworkId: String?,
    onTrustNetwork: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "netDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "dotAlpha",
    )
    val netDisplay = formatNetworkDisplay(currentNetworkId)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Trust prompt — shown once per new private network until the user answers
        if (isPrivateNetwork && !networkTrusted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NoCloudChatColors.Accent2.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    "🔒 New network detected",
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = NoCloudChatColors.TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    netDisplay,
                    fontSize = 11.sp,
                    color = NoCloudChatColors.TextMuted,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Trust this network for NoCloud Chat?",
                    fontSize = 11.sp,
                    color = NoCloudChatColors.TextMuted,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onTrustNetwork,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoCloudChatColors.OnlineGreen.copy(alpha = 0.85f),
                            contentColor   = androidx.compose.ui.graphics.Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                    ) {
                        Text("✓ Yes, trust it", fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                }
            }
            HorizontalDivider(color = NoCloudChatColors.Border, thickness = 1.dp)
        }

        // Bottom status bar — always shown
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAbout() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                when {
                    isPrivateNetwork && networkTrusted -> {
                        Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(NoCloudChatColors.OnlineGreen.copy(alpha = dotAlpha)))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("🏠 $netDisplay", fontSize = 11.sp,
                                color = NoCloudChatColors.OnlineGreen,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text("Trusted · discovery active", fontSize = 10.sp,
                                color = NoCloudChatColors.TextDim)
                        }
                    }
                    isPrivateNetwork && !networkTrusted -> {
                        Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(NoCloudChatColors.Accent2.copy(alpha = dotAlpha)))
                        Spacer(Modifier.width(6.dp))
                        Text("$netDisplay · untrusted", fontSize = 11.sp,
                            color = NoCloudChatColors.Accent2)
                    }
                    else -> {
                        Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(NoCloudChatColors.Accent2.copy(alpha = dotAlpha)))
                        Spacer(Modifier.width(6.dp))
                        Text("⚠ Not on home network", fontSize = 11.sp,
                            color = NoCloudChatColors.Accent2)
                    }
                }
            }
            Text("ℹ", fontSize = 13.sp, color = NoCloudChatColors.TextDim)
        }
    }
}

private fun formatSidebarTime(ts: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))

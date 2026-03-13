package com.nocloudchat.network

import com.nocloudchat.model.Peer
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.*
import java.util.concurrent.ConcurrentHashMap

private const val DISCOVERY_PORT        = 54321
private const val BROADCAST_INTERVAL_MS = 3_000L
private const val PEER_TIMEOUT_MS       = 12_000L
private const val MAX_PACKET_BYTES      = 512
private const val MAX_PEERS             = 32
private const val RATE_LIMIT_MAX        = 5        // max announces per window
private const val RATE_LIMIT_WINDOW_MS  = 10_000L  // per 10 seconds

class Discovery(
    private val peerId: String,
    private val messagingPort: Int,
    private val onPeersChanged: (List<Peer>) -> Unit,
    private val getSecretHash: () -> String? = { null },
    private val onSecretRequired: (String) -> Unit = {},
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private data class PeerEntry(val peer: Peer, val lastSeen: Long)

    // ConcurrentHashMap for thread-safe peer map access across coroutines
    private val peers   = ConcurrentHashMap<String, PeerEntry>()
    private val rateLimiter = RateLimiter(maxCount = RATE_LIMIT_MAX, windowMs = RATE_LIMIT_WINDOW_MS)
    private var socket: DatagramSocket? = null

    fun start() {
        socket = DatagramSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(DISCOVERY_PORT))
            broadcast = true
        }
        scope.launch { listenLoop() }
        scope.launch { broadcastLoop() }
        scope.launch { pruneLoop() }
    }

    private suspend fun listenLoop() {
        val buf = ByteArray(MAX_PACKET_BYTES + 1)  // +1 to detect oversized packets
        while (scope.isActive) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                withContext(Dispatchers.IO) { socket?.receive(packet) }

                // Drop oversized packets (security hardening)
                if (packet.length > MAX_PACKET_BYTES) continue

                val data = String(packet.data, 0, packet.length, Charsets.UTF_8)
                val ip   = packet.address?.hostAddress ?: continue

                // Rate limiting per sender IP
                if (rateLimiter.isLimited(ip)) continue

                handleAnnounce(data, ip)
            } catch (e: Exception) {
                if (!scope.isActive) break
            }
        }
    }

    private suspend fun broadcastLoop() {
        while (scope.isActive) {
            broadcast()
            delay(BROADCAST_INTERVAL_MS)
        }
    }

    private suspend fun pruneLoop() {
        while (scope.isActive) {
            delay(5_000)
            pruneStale()
            pruneRateMap()
        }
    }

    private fun broadcast() {
        val payload = buildAnnounce().toByteArray(Charsets.UTF_8)
        for (addr in getBroadcastAddresses()) {
            try {
                socket?.send(DatagramPacket(payload, payload.size, addr, DISCOVERY_PORT))
            } catch (_: Exception) {}
        }
    }

    // Privacy hardening: ANNOUNCE carries only ID + port + protocol version.
    // Display name is exchanged via TCP HELLO after discovery (see Messenger / AppState).
    private fun buildAnnounce(): String {
        val json = JSONObject()
            .put("type", "ANNOUNCE")
            .put("version", 1)
            .put("id", peerId)
            .put("port", messagingPort)
        getSecretHash()?.let { json.put("secretHash", it) }
        return json.toString()
    }

    private fun handleAnnounce(data: String, senderIP: String) {
        val json = try { JSONObject(data) } catch (_: Exception) { return }
        if (json.optString("type") != "ANNOUNCE") return
        val id   = json.optString("id").takeIf { it.isNotBlank() } ?: return
        if (id == peerId) return
        val port = json.optInt("port", 0).takeIf { it > 0 } ?: return

        // Secret filtering
        val peerSecret = json.optString("secretHash", "").takeIf { it.isNotBlank() }
        val mySecret = getSecretHash()
        when {
            mySecret != null && peerSecret == null -> return           // we require secret, peer has none
            mySecret != null && peerSecret != mySecret -> return       // secret mismatch
            mySecret == null && peerSecret != null -> {
                onSecretRequired(peerSecret)                           // prompt user
                return
            }
            // both null or both match → fall through to normal peer handling
        }

        // Cap peer table size
        if (peers.size >= MAX_PEERS && !peers.containsKey(id)) return

        val now      = System.currentTimeMillis()
        val existing = peers[id]

        val peer = Peer(
            id          = id,
            name        = existing?.peer?.name ?: "",   // name populated via HELLO handshake
            ip          = senderIP,
            port        = port,
            onlineSince = existing?.peer?.onlineSince ?: now,
        )
        peers[id] = PeerEntry(peer, now)

        if (existing == null || existing.peer.port != port || existing.peer.ip != senderIP) {
            onPeersChanged(getPeerList())
        }
    }

    private fun pruneStale() {
        val cutoff = System.currentTimeMillis() - PEER_TIMEOUT_MS
        val stale  = peers.entries.filter { it.value.lastSeen < cutoff }.map { it.key }
        if (stale.isNotEmpty()) {
            stale.forEach { peers.remove(it) }
            onPeersChanged(getPeerList())
        }
    }

    private fun pruneRateMap() {
        rateLimiter.prune(System.currentTimeMillis() - RATE_LIMIT_WINDOW_MS)
    }

    private fun getBroadcastAddresses(): List<InetAddress> {
        val result = mutableListOf<InetAddress>()
        try {
            for (iface in NetworkInterface.getNetworkInterfaces() ?: return fallback()) {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.interfaceAddresses) {
                    addr.broadcast?.let { result.add(it) }
                }
            }
        } catch (_: Exception) {}
        return result.ifEmpty { fallback() }
    }

    private fun fallback() = listOf(InetAddress.getByName("255.255.255.255"))

    fun getPeerList(): List<Peer> = peers.values.map { it.peer }

    /** Update stored name for a peer (called after HELLO handshake). */
    fun updatePeerName(peerId: String, name: String) {
        peers.computeIfPresent(peerId) { _, entry ->
            entry.copy(peer = entry.peer.copy(name = name))
        }
    }

    fun stop() {
        scope.cancel()
        try { socket?.close() } catch (_: Exception) {}
    }
}

/** Returns true if this machine has at least one RFC 1918 private IP address. */
fun isOnPrivateNetwork(): Boolean = detectNetworkId() != null

/**
 * Returns the current private network ID as a CIDR string (e.g. "192.168.1.0/24"),
 * or null if not on a private network.  Used as the stable key for the trust store.
 */
fun detectNetworkId(): String? = try {
    NetworkInterface.getNetworkInterfaces()
        ?.asSequence()
        ?.filter { it.isUp && !it.isLoopback && !it.isVirtual }
        ?.flatMap { iface -> iface.interfaceAddresses.asSequence() }
        ?.firstNotNullOfOrNull { ifAddr ->
            val addr = ifAddr.address
            if (addr !is Inet4Address) return@firstNotNullOfOrNull null
            val b = addr.address
            val isPrivate = b[0] == 10.toByte() ||
                (b[0] == 172.toByte() && (b[1].toInt() and 0xFF) in 16..31) ||
                (b[0] == 192.toByte() && b[1] == 168.toByte())
            if (!isPrivate) return@firstNotNullOfOrNull null
            val prefix = ifAddr.networkPrefixLength.toInt().coerceIn(1, 31)
            val mask   = (-1 shl (32 - prefix))
            val ipInt  = ((b[0].toInt() and 0xFF) shl 24) or
                         ((b[1].toInt() and 0xFF) shl 16) or
                         ((b[2].toInt() and 0xFF) shl 8) or
                          (b[3].toInt() and 0xFF)
            val net = ipInt and mask
            "${(net ushr 24) and 0xFF}.${(net ushr 16) and 0xFF}." +
            "${(net ushr 8)  and 0xFF}.${net and 0xFF}/$prefix"
        }
} catch (_: Exception) { null }

/**
 * Formats a network identifier for display.
 * - SSID strings are returned as-is.
 * - CIDR strings like "192.168.1.0/24" are rendered as "192.168.1.x".
 */
fun formatNetworkDisplay(networkId: String?): String {
    if (networkId == null) return "Unknown network"
    // If it looks like a CIDR, reformat; otherwise treat as SSID
    if (!networkId.contains("/")) return networkId
    val base  = networkId.substringBefore("/")
    val parts = base.split(".")
    return if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}.x" else networkId
}

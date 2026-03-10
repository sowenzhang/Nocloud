package com.nocloudchat.state

import com.nocloudchat.Preferences
import com.nocloudchat.getDownloadDirectory
import com.nocloudchat.openFileInExplorer
import com.nocloudchat.detectSsidPlatform
import com.nocloudchat.model.Message
import com.nocloudchat.model.MessageType
import com.nocloudchat.model.Peer
import com.nocloudchat.network.Discovery
import com.nocloudchat.network.FileTransfer
import com.nocloudchat.network.Messenger
import com.nocloudchat.network.detectNetworkId
import com.nocloudchat.network.isOnPrivateNetwork
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.UUID

// ─── File Transfer State ─────────────────────────────────────────────────────

data class FileTransferState(
    val transferId: String,
    val fileName: String,
    val fileSize: Long,
    val bytesTransferred: Long = 0L,
    val status: Status,
    val localPath: String? = null,
) {
    enum class Status { PENDING, SENDING, RECEIVING, COMPLETE, FAILED }
    val progress: Float get() = if (fileSize > 0) bytesTransferred.toFloat() / fileSize else 0f
}

// ─── AppState ─────────────────────────────────────────────────────────────────

class AppState {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Identity ──────────────────────────────────────────────────────────────
    val myId: String = UUID.randomUUID().toString()

    private val _myName = MutableStateFlow(
        Preferences.displayName
            ?: runCatching { System.getProperty("user.name") }.getOrNull()
            ?: "User"
    )
    val myName: StateFlow<String> = _myName.asStateFlow()

    // ── Theme ─────────────────────────────────────────────────────────────────
    private val _isDarkMode = MutableStateFlow(Preferences.isDarkMode ?: true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        scope.launch(Dispatchers.IO) { Preferences.isDarkMode = dark }
    }

    // ── Network trust ─────────────────────────────────────────────────────────
    private val _currentNetworkId = MutableStateFlow(detectSsidPlatform() ?: detectNetworkId())
    val currentNetworkId: StateFlow<String?> = _currentNetworkId.asStateFlow()

    private val _isPrivateNetwork = MutableStateFlow(isOnPrivateNetwork())
    val isPrivateNetwork: StateFlow<Boolean> = _isPrivateNetwork.asStateFlow()

    private val _networkTrusted = MutableStateFlow(
        _currentNetworkId.value?.let { Preferences.trustedNetworks.contains(it) } ?: false
    )
    val networkTrusted: StateFlow<Boolean> = _networkTrusted.asStateFlow()

    fun trustCurrentNetwork() {
        val id = _currentNetworkId.value ?: return
        scope.launch(Dispatchers.IO) {
            Preferences.trustedNetworks = Preferences.trustedNetworks + id
        }
        _networkTrusted.value = true
    }

    // ── Network secret ────────────────────────────────────────────────────────
    private val _networkSecretEnabled = MutableStateFlow(Preferences.networkSecretEnabled)
    val networkSecretEnabled: StateFlow<Boolean> = _networkSecretEnabled.asStateFlow()

    private val _networkSecretHash = MutableStateFlow<String?>(Preferences.networkSecretHash)
    val networkSecretHash: StateFlow<String?> = _networkSecretHash.asStateFlow()

    private val _secretJoinRequest = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val secretJoinRequest: SharedFlow<String> = _secretJoinRequest.asSharedFlow()

    fun setNetworkSecretEnabled(enabled: Boolean) {
        _networkSecretEnabled.value = enabled
        scope.launch(Dispatchers.IO) { Preferences.networkSecretEnabled = enabled }
    }

    fun setNetworkSecret(passphrase: String) {
        val hash = sha256(passphrase)
        _networkSecretHash.value = hash
        scope.launch(Dispatchers.IO) { Preferences.networkSecretHash = hash }
    }

    private fun sha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    // ── Peers ─────────────────────────────────────────────────────────────────
    private val _peers = MutableStateFlow<List<Peer>>(emptyList())
    val peers: StateFlow<List<Peer>> = _peers.asStateFlow()

    // ── Active chat ───────────────────────────────────────────────────────────
    private val _activePeerId = MutableStateFlow<String?>(null)
    val activePeerId: StateFlow<String?> = _activePeerId.asStateFlow()

    // ── Messages ──────────────────────────────────────────────────────────────
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // ── Unread ────────────────────────────────────────────────────────────────
    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()

    // ── File Transfers ────────────────────────────────────────────────────────
    private val _fileTransfers = MutableStateFlow<Map<String, FileTransferState>>(emptyMap())
    val fileTransfers: StateFlow<Map<String, FileTransferState>> = _fileTransfers.asStateFlow()

    private val downloadDir: File = getDownloadDirectory()

    // ── Toast ─────────────────────────────────────────────────────────────────
    data class ToastEvent(val senderName: String, val text: String)
    private val _toast = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 4)
    val toastEvents: SharedFlow<ToastEvent> = _toast.asSharedFlow()

    // ── Networking ────────────────────────────────────────────────────────────
    private var discovery: Discovery? = null
    private var messenger: Messenger? = null
    private val fileTransfer = FileTransfer(scope)

    // Tracks resolved display names received via HELLO handshake
    private val resolvedNames = mutableMapOf<String, String>()

    // Persists last-known names so the UI can show them even after a peer goes offline
    private val _peerNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val peerNames: StateFlow<Map<String, String>> = _peerNames.asStateFlow()

    // Tracks which peer IDs we've already sent a HELLO to this session
    private val helloSentTo = mutableSetOf<String>()

    init {
        scope.launch { initNetworking() }
        // Re-check network identity every 30 s (handles Wi-Fi switching)
        scope.launch {
            while (isActive) {
                delay(30_000)
                val newNetId = detectSsidPlatform() ?: detectNetworkId()
                if (newNetId != _currentNetworkId.value) {
                    _currentNetworkId.value = newNetId
                    _networkTrusted.value =
                        newNetId?.let { Preferences.trustedNetworks.contains(it) } ?: false
                }
                _isPrivateNetwork.value = isOnPrivateNetwork()
            }
        }
    }

    private suspend fun initNetworking() {
        messenger = Messenger(myId) { msg -> scope.launch { handleIncoming(msg) } }
        val port = messenger!!.start()

        discovery = Discovery(
            peerId           = myId,
            messagingPort    = port,
            onPeersChanged   = { updated -> handlePeersChanged(updated) },
            getSecretHash    = { if (_networkSecretEnabled.value) _networkSecretHash.value else null },
            onSecretRequired = { hash -> scope.launch { _secretJoinRequest.emit(hash) } },
        )
        discovery!!.start()
    }

    private fun handlePeersChanged(updated: List<Peer>) {
        // Overlay any already-resolved display names
        val withNames = updated.map { p ->
            p.copy(name = resolvedNames[p.id] ?: p.name.ifBlank { "Connecting…" })
        }
        _peers.value = withNames

        // Send HELLO to newly discovered peers
        val updatedIds = updated.map { it.id }.toSet()
        helloSentTo.retainAll(updatedIds)   // drop IDs of peers that left

        for (peer in updated) {
            if (peer.id !in helloSentTo) {
                helloSentTo.add(peer.id)
                scope.launch {
                    try { messenger?.sendMessage(peer, buildHello(peer.id)) }
                    catch (_: Exception) {}
                }
            }
        }
    }

    private fun buildHello(toPeerId: String) = Message(
        id        = UUID.randomUUID().toString(),
        fromId    = myId,
        fromName  = _myName.value,
        toId      = toPeerId,
        text      = "",
        timestamp = System.currentTimeMillis(),
        incoming  = false,
        type      = MessageType.HELLO,
    )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun openChat(peerId: String) {
        _activePeerId.value = peerId
        _unreadCounts.value = _unreadCounts.value - peerId
    }

    fun closeChat() {
        _activePeerId.value = null
    }

    suspend fun sendMessage(peerId: String, text: String) {
        val peer = _peers.value.find { it.id == peerId }
            ?: throw IllegalStateException("Peer $peerId not found")
        val msg = Message(
            id        = UUID.randomUUID().toString(),
            fromId    = myId,
            fromName  = _myName.value,
            toId      = peerId,
            text      = text.trim(),
            timestamp = System.currentTimeMillis(),
            incoming  = false,
        )
        messenger?.sendMessage(peer, msg) ?: throw IllegalStateException("Messenger not started")
        appendMessage(peerId, msg)
    }

    suspend fun sendFile(peerId: String, file: File) {
        val peer = _peers.value.find { it.id == peerId }
            ?: throw IllegalStateException("Peer $peerId not found")

        val transferId = UUID.randomUUID().toString()

        updateTransfer(FileTransferState(
            transferId = transferId,
            fileName   = file.name,
            fileSize   = file.length(),
            status     = FileTransferState.Status.SENDING,
        ))

        val port = fileTransfer.prepareSend(
            transferId = transferId,
            file       = file,
            onProgress = { id, bytes -> patchTransfer(id) { copy(bytesTransferred = bytes) } },
            onComplete = { id, path  -> patchTransfer(id) { copy(status = FileTransferState.Status.COMPLETE, localPath = path) } },
            onFailed   = { id, _     -> patchTransfer(id) { copy(status = FileTransferState.Status.FAILED) } },
        )

        val msg = Message(
            id           = transferId,
            fromId       = myId,
            fromName     = _myName.value,
            toId         = peerId,
            text         = "",
            timestamp    = System.currentTimeMillis(),
            incoming     = false,
            type         = MessageType.FILE_OFFER,
            fileName     = file.name,
            fileSize     = file.length(),
            transferPort = port,
        )
        messenger?.sendMessage(peer, msg) ?: throw IllegalStateException("Messenger not started")
        appendMessage(peerId, msg)
    }

    fun acceptFileOffer(msg: Message) {
        val peer = _peers.value.find { it.id == msg.fromId } ?: return
        val transferId = msg.id

        updateTransfer(FileTransferState(
            transferId = transferId,
            fileName   = msg.fileName ?: "file",
            fileSize   = msg.fileSize ?: 0L,
            status     = FileTransferState.Status.RECEIVING,
        ))

        fileTransfer.receive(
            transferId  = transferId,
            senderIp    = peer.ip,
            senderPort  = msg.transferPort ?: return,
            filename    = msg.fileName ?: "file",
            destDir     = downloadDir,
            onProgress  = { id, bytes -> patchTransfer(id) { copy(bytesTransferred = bytes) } },
            onComplete  = { id, path  -> patchTransfer(id) { copy(status = FileTransferState.Status.COMPLETE, localPath = path, bytesTransferred = fileSize) } },
            onFailed    = { id, _     -> patchTransfer(id) { copy(status = FileTransferState.Status.FAILED) } },
        )
    }

    fun openLocalFile(path: String) {
        scope.launch(Dispatchers.IO) {
            openFileInExplorer(path)
        }
    }

    suspend fun setDisplayName(name: String) {
        val trimmed = name.trim().take(32)
        if (trimmed.isBlank()) return
        _myName.value = trimmed
        withContext(Dispatchers.IO) { Preferences.displayName = trimmed }
        // Re-broadcast our new name to all known peers via HELLO
        val currentPeers = _peers.value
        helloSentTo.clear()
        for (peer in currentPeers) {
            helloSentTo.add(peer.id)
            scope.launch {
                try { messenger?.sendMessage(peer, buildHello(peer.id)) }
                catch (_: Exception) {}
            }
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private suspend fun handleIncoming(msg: Message) {
        when (msg.type) {
            MessageType.HELLO -> {
                // Update resolved name and peer list — don't add to chat
                resolvedNames[msg.fromId] = msg.fromName
                _peerNames.value = resolvedNames.toMap()
                _peers.value = _peers.value.map { p ->
                    if (p.id == msg.fromId) p.copy(name = msg.fromName) else p
                }
                // Reciprocate if we haven't yet
                if (msg.fromId !in helloSentTo) {
                    val peer = _peers.value.find { it.id == msg.fromId }
                    if (peer != null) {
                        helloSentTo.add(msg.fromId)
                        scope.launch {
                            try { messenger?.sendMessage(peer, buildHello(peer.id)) }
                            catch (_: Exception) {}
                        }
                    }
                }
                return
            }
            else -> { /* handled below */ }
        }

        val fromId = msg.fromId
        appendMessage(fromId, msg)

        if (_activePeerId.value != fromId) {
            _unreadCounts.value = _unreadCounts.value.toMutableMap().also {
                it[fromId] = (it[fromId] ?: 0) + 1
            }
            val senderName = _peers.value.find { it.id == fromId }?.name ?: msg.fromName
            val toastText = when (msg.type) {
                MessageType.TEXT       -> msg.text
                MessageType.FILE_OFFER -> "📎 ${msg.fileName ?: "file"} (${formatFileSize(msg.fileSize ?: 0)})"
                MessageType.HELLO      -> return
            }
            _toast.emit(ToastEvent(senderName, toastText))
        }
    }

    private fun appendMessage(peerId: String, msg: Message) {
        val current = _messages.value.getOrDefault(peerId, emptyList())
        _messages.value = _messages.value.toMutableMap().also { it[peerId] = current + msg }
    }

    private fun updateTransfer(state: FileTransferState) {
        _fileTransfers.value = _fileTransfers.value.toMutableMap().also { it[state.transferId] = state }
    }

    private fun patchTransfer(id: String, patch: FileTransferState.() -> FileTransferState) {
        _fileTransfers.value = _fileTransfers.value.toMutableMap().also { map ->
            map[id]?.let { map[id] = it.patch() }
        }
    }

    fun shutdown() {
        discovery?.stop()
        messenger?.stop()
        scope.cancel()
    }
}

fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024         -> "$bytes B"
    bytes < 1_048_576     -> "${bytes / 1_024} KB"
    bytes < 1_073_741_824 -> "${"%.1f".format(bytes.toFloat() / 1_048_576)} MB"
    else                  -> "${"%.1f".format(bytes.toFloat() / 1_073_741_824)} GB"
}

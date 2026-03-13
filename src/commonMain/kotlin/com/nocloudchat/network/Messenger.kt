package com.nocloudchat.network

import com.nocloudchat.model.Message
import com.nocloudchat.model.Peer
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.*

private const val CONNECT_TIMEOUT_MS = 5_000

class Messenger(
    private val peerId: String,
    private val onMessage: (Message) -> Unit,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null

    suspend fun start(): Int = withContext(Dispatchers.IO) {
        val ss = ServerSocket(0)
        serverSocket = ss
        scope.launch { acceptLoop(ss) }
        ss.localPort
    }

    private suspend fun acceptLoop(ss: ServerSocket) {
        while (scope.isActive) {
            try {
                val socket = withContext(Dispatchers.IO) { ss.accept() }
                scope.launch { handleSocket(socket) }
            } catch (_: Exception) {
                if (!scope.isActive) break
            }
        }
    }

    private fun handleSocket(socket: Socket) {
        socket.use {
            try {
                val din = DataInputStream(BufferedInputStream(it.getInputStream()))
                val length = din.readInt()
                if (length <= 0 || length > MAX_MESSAGE_BYTES) return
                val bytes = ByteArray(length)
                din.readFully(bytes)
                val json = JSONObject(String(bytes, Charsets.UTF_8))
                val msg = parseMessage(json) ?: return
                onMessage(msg)
            } catch (_: Exception) {}
        }
    }

    suspend fun sendMessage(peer: Peer, message: Message) = withContext(Dispatchers.IO) {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(peer.ip, peer.port), CONNECT_TIMEOUT_MS)
            val bytes = messageToJson(message).toByteArray(Charsets.UTF_8)
            val dout = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))
            dout.writeInt(bytes.size)
            dout.write(bytes)
            dout.flush()
        }
    }

    fun stop() {
        scope.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
    }
}

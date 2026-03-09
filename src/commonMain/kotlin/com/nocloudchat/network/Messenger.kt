package com.nocloudchat.network

import com.nocloudchat.model.Message
import com.nocloudchat.model.MessageType
import com.nocloudchat.model.Peer
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.*

private const val MAX_MESSAGE_BYTES = 10 * 1024 * 1024 // 10 MB
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

    private fun messageToJson(msg: Message): String {
        val json = JSONObject()
            .put("id", msg.id)
            .put("from", msg.fromId)
            .put("fromName", msg.fromName)
            .put("to", msg.toId)
            .put("timestamp", msg.timestamp)

        when (msg.type) {
            MessageType.TEXT -> json
                .put("type", "text")
                .put("text", msg.text)
            MessageType.FILE_OFFER -> json
                .put("type", "file_offer")
                .put("fileName", msg.fileName ?: "")
                .put("fileSize", msg.fileSize ?: 0L)
                .put("transferPort", msg.transferPort ?: 0)
            MessageType.HELLO -> json
                .put("type", "hello")
        }
        return json.toString()
    }

    private fun parseMessage(json: JSONObject): Message? = try {
        val id = json.getString("id")
        val fromId = json.getString("from")
        val fromName = json.optString("fromName", "Unknown")
        val toId = json.getString("to")
        val timestamp = json.getLong("timestamp")

        when (json.optString("type")) {
            "text" -> Message(
                id = id, fromId = fromId, fromName = fromName, toId = toId,
                text = json.getString("text"),
                timestamp = timestamp, incoming = true,
                type = MessageType.TEXT,
            )
            "file_offer" -> Message(
                id = id, fromId = fromId, fromName = fromName, toId = toId,
                text = "",
                timestamp = timestamp, incoming = true,
                type = MessageType.FILE_OFFER,
                fileName = json.optString("fileName").takeIf { it.isNotBlank() },
                fileSize = json.optLong("fileSize").takeIf { it > 0 },
                transferPort = json.optInt("transferPort").takeIf { it > 0 },
            )
            "hello" -> Message(
                id = id, fromId = fromId, fromName = fromName, toId = toId,
                text = "", timestamp = timestamp, incoming = true,
                type = MessageType.HELLO,
            )
            else -> null
        }
    } catch (_: Exception) { null }

    fun stop() {
        scope.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
    }
}

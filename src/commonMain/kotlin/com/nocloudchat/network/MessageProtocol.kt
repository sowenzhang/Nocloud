package com.nocloudchat.network

import com.nocloudchat.model.Message
import com.nocloudchat.model.MessageType
import org.json.JSONObject

internal const val MAX_MESSAGE_BYTES = 10 * 1024 * 1024 // 10 MB

/** Serialises a [Message] to a JSON string for transmission over the wire. */
internal fun messageToJson(msg: Message): String {
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

/** Parses a [JSONObject] received from the wire into a [Message], or null if invalid. */
internal fun parseMessage(json: JSONObject): Message? = try {
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

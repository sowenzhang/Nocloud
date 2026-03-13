package com.nocloudchat.network

import com.nocloudchat.model.Message
import com.nocloudchat.model.MessageType
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MessageProtocolTest {

    private val baseId = "test-id-123"
    private val fromId = "peer-a"
    private val fromName = "Alice"
    private val toId = "peer-b"
    private val timestamp = 1_700_000_000_000L

    // ── parseMessage ─────────────────────────────────────────────────────────

    @Test
    fun `parseMessage returns TEXT message for valid text JSON`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId).put("fromName", fromName)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "text").put("text", "Hello!")

        val msg = parseMessage(json)

        assertNotNull(msg)
        assertEquals(MessageType.TEXT, msg.type)
        assertEquals("Hello!", msg.text)
        assertEquals(fromId, msg.fromId)
        assertEquals(fromName, msg.fromName)
        assertEquals(toId, msg.toId)
        assertEquals(timestamp, msg.timestamp)
        assertEquals(true, msg.incoming)
    }

    @Test
    fun `parseMessage returns FILE_OFFER message with all fields`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId).put("fromName", fromName)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "file_offer").put("fileName", "photo.png")
            .put("fileSize", 2048L).put("transferPort", 9876)

        val msg = parseMessage(json)

        assertNotNull(msg)
        assertEquals(MessageType.FILE_OFFER, msg.type)
        assertEquals("photo.png", msg.fileName)
        assertEquals(2048L, msg.fileSize)
        assertEquals(9876, msg.transferPort)
    }

    @Test
    fun `parseMessage returns HELLO message`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId).put("fromName", fromName)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "hello")

        val msg = parseMessage(json)

        assertNotNull(msg)
        assertEquals(MessageType.HELLO, msg.type)
        assertEquals("", msg.text)
    }

    @Test
    fun `parseMessage returns null for unknown type`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "unknown")

        assertNull(parseMessage(json))
    }

    @Test
    fun `parseMessage returns null when required field is missing`() {
        val json = JSONObject()
            .put("from", fromId).put("to", toId).put("timestamp", timestamp)
            .put("type", "text").put("text", "Hi")
        // "id" is missing

        assertNull(parseMessage(json))
    }

    @Test
    fun `parseMessage falls back to 'Unknown' when fromName is absent`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "text").put("text", "Hi")

        val msg = parseMessage(json)

        assertNotNull(msg)
        assertEquals("Unknown", msg.fromName)
    }

    @Test
    fun `parseMessage treats blank fileName as null`() {
        val json = JSONObject()
            .put("id", baseId).put("from", fromId).put("fromName", fromName)
            .put("to", toId).put("timestamp", timestamp)
            .put("type", "file_offer").put("fileName", "")
            .put("fileSize", 0L).put("transferPort", 0)

        val msg = parseMessage(json)

        assertNotNull(msg)
        assertNull(msg.fileName)
        assertNull(msg.fileSize)
        assertNull(msg.transferPort)
    }

    // ── messageToJson ─────────────────────────────────────────────────────────

    @Test
    fun `messageToJson serialises TEXT message correctly`() {
        val msg = Message(
            id = baseId, fromId = fromId, fromName = fromName,
            toId = toId, text = "Hello!", timestamp = timestamp,
            incoming = false, type = MessageType.TEXT,
        )

        val json = JSONObject(messageToJson(msg))

        assertEquals(baseId, json.getString("id"))
        assertEquals(fromId, json.getString("from"))
        assertEquals(fromName, json.getString("fromName"))
        assertEquals(toId, json.getString("to"))
        assertEquals(timestamp, json.getLong("timestamp"))
        assertEquals("text", json.getString("type"))
        assertEquals("Hello!", json.getString("text"))
    }

    @Test
    fun `messageToJson serialises FILE_OFFER message correctly`() {
        val msg = Message(
            id = baseId, fromId = fromId, fromName = fromName,
            toId = toId, text = "", timestamp = timestamp,
            incoming = false, type = MessageType.FILE_OFFER,
            fileName = "report.pdf", fileSize = 512_000L, transferPort = 8765,
        )

        val json = JSONObject(messageToJson(msg))

        assertEquals("file_offer", json.getString("type"))
        assertEquals("report.pdf", json.getString("fileName"))
        assertEquals(512_000L, json.getLong("fileSize"))
        assertEquals(8765, json.getInt("transferPort"))
    }

    @Test
    fun `messageToJson serialises HELLO message correctly`() {
        val msg = Message(
            id = baseId, fromId = fromId, fromName = fromName,
            toId = toId, text = "", timestamp = timestamp,
            incoming = false, type = MessageType.HELLO,
        )

        val json = JSONObject(messageToJson(msg))

        assertEquals("hello", json.getString("type"))
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `TEXT message survives a serialise-then-parse round-trip`() {
        val original = Message(
            id = baseId, fromId = fromId, fromName = fromName,
            toId = toId, text = "Round-trip test 🎉", timestamp = timestamp,
            incoming = false, type = MessageType.TEXT,
        )

        val parsed = parseMessage(JSONObject(messageToJson(original)))

        assertNotNull(parsed)
        assertEquals(original.id, parsed.id)
        assertEquals(original.fromId, parsed.fromId)
        assertEquals(original.fromName, parsed.fromName)
        assertEquals(original.toId, parsed.toId)
        assertEquals(original.text, parsed.text)
        assertEquals(original.timestamp, parsed.timestamp)
        assertEquals(MessageType.TEXT, parsed.type)
    }

    @Test
    fun `FILE_OFFER message survives a serialise-then-parse round-trip`() {
        val original = Message(
            id = baseId, fromId = fromId, fromName = fromName,
            toId = toId, text = "", timestamp = timestamp,
            incoming = false, type = MessageType.FILE_OFFER,
            fileName = "video.mp4", fileSize = 104_857_600L, transferPort = 12345,
        )

        val parsed = parseMessage(JSONObject(messageToJson(original)))

        assertNotNull(parsed)
        assertEquals(original.fileName, parsed.fileName)
        assertEquals(original.fileSize, parsed.fileSize)
        assertEquals(original.transferPort, parsed.transferPort)
    }

    @Test
    fun `HELLO message survives a serialise-then-parse round-trip`() {
        val original = Message(
            id = baseId, fromId = fromId, fromName = "Bob",
            toId = toId, text = "", timestamp = timestamp,
            incoming = false, type = MessageType.HELLO,
        )

        val parsed = parseMessage(JSONObject(messageToJson(original)))

        assertNotNull(parsed)
        assertEquals(MessageType.HELLO, parsed.type)
        assertEquals("Bob", parsed.fromName)
    }
}

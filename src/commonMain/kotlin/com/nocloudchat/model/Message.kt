package com.nocloudchat.model

enum class MessageType { TEXT, FILE_OFFER, HELLO }

data class Message(
    val id: String,
    val fromId: String,
    val fromName: String,
    val toId: String,
    val text: String,
    val timestamp: Long,
    val incoming: Boolean,
    val type: MessageType = MessageType.TEXT,
    // FILE_OFFER fields
    val fileName: String? = null,
    val fileSize: Long? = null,
    val transferPort: Int? = null,
)

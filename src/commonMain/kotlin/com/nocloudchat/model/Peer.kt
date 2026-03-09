package com.nocloudchat.model

data class Peer(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int,
    val onlineSince: Long = System.currentTimeMillis(),
)

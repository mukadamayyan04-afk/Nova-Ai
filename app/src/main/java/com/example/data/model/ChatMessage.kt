package com.example.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    NOVA,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false,
    val commandExecuted: String? = null
)

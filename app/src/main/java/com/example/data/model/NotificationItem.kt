package com.example.data.model

data class NotificationItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

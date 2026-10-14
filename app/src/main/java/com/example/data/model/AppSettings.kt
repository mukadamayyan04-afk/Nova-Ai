package com.example.data.model

enum class AssistantLanguage(val label: String, val speechLocaleCode: String) {
    HINGLISH("Hinglish (Hindi + English)", "hi-IN"),
    HINDI("हिंदी (Hindi)", "hi-IN"),
    ENGLISH("English (India/Global)", "en-IN")
}

data class AppSettings(
    val language: AssistantLanguage = AssistantLanguage.HINGLISH,
    val speechRate: Float = 1.05f,
    val speechPitch: Float = 1.0f,
    val voiceOutputEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val notificationAnnounceEnabled: Boolean = true,
    val readMessageContentEnabled: Boolean = true,
    val reducedAnimationMode: Boolean = false,
    val customGeminiApiKey: String = ""
)

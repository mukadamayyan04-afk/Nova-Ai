package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.AssistantLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class NovaTextToSpeech(
    context: Context,
    private val onSpeakingStarted: () -> Unit,
    private val onSpeakingFinished: () -> Unit
) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private var currentLanguage = AssistantLanguage.HINGLISH
    private var currentRate = 1.05f
    private var currentPitch = 1.0f

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                applySettings(currentLanguage, currentRate, currentPitch)
                setupProgressListener()
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                onSpeakingStarted()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeakingFinished()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeakingFinished()
            }
        })
    }

    fun applySettings(language: AssistantLanguage, rate: Float, pitch: Float) {
        currentLanguage = language
        currentRate = rate
        currentPitch = pitch

        if (!isInitialized) return

        tts?.let { engine ->
            engine.setSpeechRate(rate)
            engine.setPitch(pitch)

            val locale = when (language) {
                AssistantLanguage.HINDI -> Locale.forLanguageTag("hi-IN")
                AssistantLanguage.HINGLISH -> Locale.forLanguageTag("hi-IN")
                AssistantLanguage.ENGLISH -> Locale.forLanguageTag("en-IN")
            }

            val result = engine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                engine.setLanguage(Locale.ENGLISH)
            }
        }
    }

    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized) return
        stop()

        // Clean out markdown symbols for clean spoken speech
        val cleaned = text
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("`([^`]+)`"), "$1")
            .replace(Regex("#+\\s*"), "")
            .replace("https://", "")
            .replace("http://", "")
            .trim()

        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (!isInitialized) return
        tts?.stop()
        _isSpeaking.value = false
        onSpeakingFinished()
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}

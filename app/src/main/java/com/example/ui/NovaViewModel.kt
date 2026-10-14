package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.model.AppSettings
import com.example.data.model.AssistantLanguage
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.NotificationItem
import com.example.service.NovaNotificationHub
import com.example.util.NovaAppLauncher
import com.example.util.NovaSoundEffects
import com.example.util.NovaSpeechRecognizer
import com.example.util.NovaTextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // State flows
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState = _assistantState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude = _audioAmplitude.asStateFlow()

    private val _liveSpeechText = MutableStateFlow("")
    val liveSpeechText = _liveSpeechText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Engines
    private val tts: NovaTextToSpeech = NovaTextToSpeech(
        context = context,
        onSpeakingStarted = {
            _assistantState.value = AssistantState.SPEAKING
        },
        onSpeakingFinished = {
            if (_assistantState.value == AssistantState.SPEAKING) {
                _assistantState.value = AssistantState.IDLE
            }
        }
    )

    private val speechRecognizer: NovaSpeechRecognizer = NovaSpeechRecognizer(
        context = context,
        onSpeechResult = { recognizedText ->
            _liveSpeechText.value = recognizedText
            processUserInput(recognizedText)
        },
        onPartialResult = { partial ->
            _liveSpeechText.value = partial
        },
        onAmplitudeChanged = { amp ->
            _audioAmplitude.value = amp
        },
        onErrorOccurred = { err ->
            _errorMessage.value = err
            _assistantState.value = AssistantState.IDLE
            NovaSoundEffects.playError(_settings.value.soundEffectsEnabled)
        },
        onListeningStateChanged = { listening ->
            if (listening) {
                _assistantState.value = AssistantState.LISTENING
            } else if (_assistantState.value == AssistantState.LISTENING) {
                _assistantState.value = AssistantState.IDLE
            }
        }
    )

    init {
        // Initial greeting
        val initialGreeting = ChatMessage(
            text = "Systems online. Namaste Boss! Main NOVA hoon, aapka personal AI assistant. Bolo, main aapki kya madad kar sakta hoon?",
            sender = MessageSender.NOVA
        )
        _chatMessages.value = listOf(initialGreeting)

        // Observe notification listener
        viewModelScope.launch {
            NovaNotificationHub.incomingNotifications.collect { item ->
                handleIncomingNotification(item)
            }
        }
    }

    fun startListening() {
        if (_assistantState.value == AssistantState.SPEAKING) {
            tts.stop()
        }
        _liveSpeechText.value = ""
        _errorMessage.value = null
        NovaSoundEffects.playListenStart(_settings.value.soundEffectsEnabled)
        speechRecognizer.startListening(_settings.value.language)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun toggleListening() {
        if (_assistantState.value == AssistantState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun submitTextMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        _liveSpeechText.value = trimmed
        processUserInput(trimmed)
    }

    private fun processUserInput(rawText: String) {
        val userText = rawText.trim()
        if (userText.isEmpty()) return

        // Add user message to conversation
        val userMsg = ChatMessage(text = userText, sender = MessageSender.USER)
        _chatMessages.value = _chatMessages.value + userMsg

        val clean = userText.lowercase().trim()

        // 1. Wake-word check ("Nova", "Hey Nova", "Nova bro")
        if (clean == "nova" || clean == "hey nova" || clean == "hi nova" || clean == "namaste nova" || clean == "hello nova") {
            handleWakeWordResponse()
            return
        }

        // 2. Android App / URL commands
        val commandResult = NovaAppLauncher.executeIfCommand(context, userText)
        if (commandResult != null && commandResult.handled) {
            NovaSoundEffects.playSuccess(_settings.value.soundEffectsEnabled, viewModelScope)
            val novaMsg = ChatMessage(
                text = commandResult.spokenResponse,
                sender = MessageSender.NOVA,
                commandExecuted = commandResult.commandLabel
            )
            _chatMessages.value = _chatMessages.value + novaMsg

            if (_settings.value.voiceOutputEnabled) {
                tts.speak(commandResult.spokenResponse)
            }
            return
        }

        // 3. AI Query via Gemini
        _assistantState.value = AssistantState.PROCESSING
        _errorMessage.value = null

        viewModelScope.launch {
            val result = GeminiClient.sendMessage(
                history = _chatMessages.value,
                userMessage = userText,
                customApiKey = _settings.value.customGeminiApiKey
            )

            result.onSuccess { responseText ->
                NovaSoundEffects.playSuccess(_settings.value.soundEffectsEnabled, viewModelScope)
                val replyMsg = ChatMessage(
                    text = responseText,
                    sender = MessageSender.NOVA
                )
                _chatMessages.value = _chatMessages.value + replyMsg

                if (_settings.value.voiceOutputEnabled) {
                    tts.speak(responseText)
                } else {
                    _assistantState.value = AssistantState.IDLE
                }
            }.onFailure { ex ->
                NovaSoundEffects.playError(_settings.value.soundEffectsEnabled)
                val errMsg = ex.message ?: "Unknown error occurred"
                _errorMessage.value = errMsg
                val systemMsg = ChatMessage(
                    text = "⚠️ $errMsg",
                    sender = MessageSender.SYSTEM
                )
                _chatMessages.value = _chatMessages.value + systemMsg
                _assistantState.value = AssistantState.ERROR
            }
        }
    }

    private fun handleWakeWordResponse() {
        val wakeReply = "Haan bro, bolo? Ready for command."
        NovaSoundEffects.playWakeWord(_settings.value.soundEffectsEnabled, viewModelScope)
        val novaMsg = ChatMessage(
            text = wakeReply,
            sender = MessageSender.NOVA,
            commandExecuted = "Wake Word: Nova"
        )
        _chatMessages.value = _chatMessages.value + novaMsg

        if (_settings.value.voiceOutputEnabled) {
            tts.speak(wakeReply)
        }
    }

    private fun handleIncomingNotification(item: NotificationItem) {
        _notifications.value = listOf(item) + _notifications.value.take(49)

        if (!_settings.value.notificationAnnounceEnabled) return

        val announcementText = if (_settings.value.readMessageContentEnabled && item.title.isNotEmpty()) {
            "Bro, ${item.appName} se ek naya message aaya hai. ${item.title} kehta hai: ${item.content}"
        } else {
            "Bro, tumhe ek naya message aaya hai ${item.appName} se."
        }

        val notifyMsg = ChatMessage(
            text = "🔔 Notification Announcement: $announcementText",
            sender = MessageSender.SYSTEM
        )
        _chatMessages.value = _chatMessages.value + notifyMsg

        if (_settings.value.voiceOutputEnabled) {
            tts.speak(announcementText)
        }
    }

    fun speakText(text: String) {
        tts.speak(text)
    }

    fun stopSpeaking() {
        tts.stop()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun clearChatHistory() {
        tts.stop()
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Memory reset complete. Ready for new commands, Boss.",
                sender = MessageSender.NOVA
            )
        )
        _errorMessage.value = null
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        tts.applySettings(newSettings.language, newSettings.speechRate, newSettings.speechPitch)
    }

    fun clearError() {
        _errorMessage.value = null
        if (_assistantState.value == AssistantState.ERROR) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        tts.shutdown()
    }
}

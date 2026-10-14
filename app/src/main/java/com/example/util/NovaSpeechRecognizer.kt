package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.data.model.AssistantLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NovaSpeechRecognizer(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onAmplitudeChanged: (Float) -> Unit,
    private val onErrorOccurred: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Builds a bilingual/multilingual RecognizerIntent tuned for Indian Hinglish, Hindi, and English.
     */
    fun createRecognizerIntent(language: AssistantLanguage): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

            when (language) {
                AssistantLanguage.HINGLISH -> {
                    // Primary language Hindi (India) with English (India & US) as additional languages
                    // to facilitate seamless code-switching in Hinglish sentences.
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "en-US", "hi-IN"))
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                }
                AssistantLanguage.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
                }
                AssistantLanguage.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en-GB"))
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                }
            }
        }
    }

    fun startListening(language: AssistantLanguage) {
        if (!isAvailable()) {
            onErrorOccurred("Speech recognition is not available or Google Speech Services is disabled on this device.")
            return
        }

        try {
            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        onListeningStateChanged(true)
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS (usually -2 to 10 dB) to 0.0f..1.0f for the Arc Reactor visualizer
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        onAmplitudeChanged(normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        onListeningStateChanged(false)
                        onAmplitudeChanged(0f)
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        onListeningStateChanged(false)
                        onAmplitudeChanged(0f)

                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                            SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection required for bilingual speech engine."
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection timed out."
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech engine is busy. Retrying..."
                            SpeechRecognizer.ERROR_SERVER -> "Google Speech recognition server error."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to speak."
                            else -> "Voice recognition error (code $error)"
                        }

                        // Avoid noisy alerts for harmless timeouts
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            onErrorOccurred(message)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        onListeningStateChanged(false)
                        onAmplitudeChanged(0f)

                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotEmpty()) {
                            onSpeechResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim() ?: ""
                        if (partial.isNotEmpty()) {
                            onPartialResult(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = createRecognizerIntent(language)
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onListeningStateChanged(false)
            onErrorOccurred("Could not initialize SpeechRecognizer: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        onListeningStateChanged(false)
        onAmplitudeChanged(0f)
    }

    fun destroy() {
        stopListening()
    }
}

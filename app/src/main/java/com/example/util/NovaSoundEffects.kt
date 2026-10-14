package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object NovaSoundEffects {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            // Ignore if tone generator unavailable in container or some emulators
        }
    }

    fun playListenStart(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (_: Exception) {}
    }

    fun playSuccess(enabled: Boolean, scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Default) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
            } catch (_: Exception) {}
        }
    }

    fun playWakeWord(enabled: Boolean, scope: CoroutineScope) {
        if (!enabled) return
        scope.launch(Dispatchers.Default) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 140)
            } catch (_: Exception) {}
        }
    }

    fun playError(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
        } catch (_: Exception) {}
    }
}

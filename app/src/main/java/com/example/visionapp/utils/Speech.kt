package com.example.visionapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.visionapp.LocalizationConfig
import java.util.*


class TextToSpeechManager(
    private val context: Context,
    private val language: Locale = LocalizationConfig.DEFAULT_TTS_LANGUAGE,
    private val onReady: (() -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(language)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                throw IllegalStateException("Chosen language is not supported")
            } else {
                isReady = true
                onReady?.invoke()
            }
        } else {
            throw IllegalStateException("Failed to initialize TTS")
        }
    }

    fun speak(text: String) {
        if (text.isBlank() || !isReady) return

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        /**
         * Immediately stops any ongoing speech output.
         */
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}

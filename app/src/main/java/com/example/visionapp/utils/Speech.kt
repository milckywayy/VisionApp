package com.example.visionapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.visionapp.LocalizationConfig
import com.example.visionapp.communiates.CommunicateQueue
import com.example.visionapp.model.SpeechPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*


class TextToSpeechManager(
    private val context: Context,
    private val language: Locale = LocalizationConfig.DEFAULT_TTS_LANGUAGE,
    private val onReady: (() -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

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

                startQueueMonitor()
            }
        } else {
            throw IllegalStateException("Failed to initialize TTS")
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun speakNextFromQueue(){
        if(CommunicateQueue.isEmpty()) return

        val communicate = CommunicateQueue.poll()
        val message = communicate?.communicateType?.message
        if (!message.isNullOrEmpty()){
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, message.hashCode().toString())
        }
    }

    fun speak(text: String, priority: SpeechPriority = SpeechPriority.NORMAL) {
        if (text.isBlank() || !isReady) return

        val queueMode = when (priority) {
            SpeechPriority.IMPORTANT -> TextToSpeech.QUEUE_FLUSH
            SpeechPriority.NORMAL -> TextToSpeech.QUEUE_ADD
        }

        tts?.speak(text, queueMode, null, text.hashCode().toString())
    }

    fun stop() {
        /**
         * Immediately stops any ongoing speech output.
         */
        tts?.stop()
    }

    fun shutdown() {
        job.cancel()
        tts?.shutdown()
    }

    private fun startQueueMonitor() {
        scope.launch {
            while (isActive) {
                delay(100)
                if (!isSpeaking() && !CommunicateQueue.isEmpty()) {
                    speakNextFromQueue()
                }
            }
        }
    }
}

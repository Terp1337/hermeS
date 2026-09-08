package com.hermes.assistant

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Kapselt den Android SpeechRecognizer und stellt Compose-State bereit:
 * - isListening: ob gerade zugehört wird
 * - partialText / finalText: laufende bzw. fertige Erkennung
 * - rmsLevel: aktuelle Lautstärke (0..~10), für die Wellenform-Animation im HUD
 */
class SpeechRecognitionManager(private val context: Context) {

    var isListening = mutableStateOf(false)
        private set
    var partialText = mutableStateOf("")
        private set
    var finalText = mutableStateOf("")
        private set
    var rmsLevel = mutableFloatStateOf(0f)
        private set

    private var onResult: ((String) -> Unit)? = null

    private val recognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {
            isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            // rmsdB liegt grob zwischen -2 (Stille) und 10 (laut) -> für UI normalisieren
            rmsLevel.floatValue = rmsdB.coerceIn(0f, 10f)
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            val matches = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partialText.value = matches?.firstOrNull().orEmpty()
        }

        override fun onResults(results: android.os.Bundle?) {
            val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull().orEmpty()
            finalText.value = text
            isListening.value = false
            rmsLevel.floatValue = 0f
            if (text.isNotBlank()) onResult?.invoke(text)
        }

        override fun onError(error: Int) {
            isListening.value = false
            rmsLevel.floatValue = 0f
        }

        override fun onEndOfSpeech() {
            isListening.value = false
            rmsLevel.floatValue = 0f
        }

        override fun onBeginningOfSpeech() {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    }

    fun startListening(onResult: (String) -> Unit) {
        this.onResult = onResult
        partialText.value = ""
        finalText.value = ""

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        recognizer.startListening(intent)
    }

    fun stopListening() {
        recognizer.stopListening()
    }

    fun destroy() {
        recognizer.destroy()
    }
}

package com.hermes.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hermes.assistant.ui.JarvisOrb

class MainActivity : ComponentActivity() {

    private lateinit var speech: SpeechRecognitionManager

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) speech.startListening { handleCommand(it) }
    }

    private var lastCommand by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speech = SpeechRecognitionManager(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    HermesScreen(
                        isListening = speech.isListening.value,
                        partialText = speech.partialText.value,
                        finalText = lastCommand,
                        rmsLevel = speech.rmsLevel.floatValue,
                        onOrbTap = { onOrbTapped() }
                    )
                }
            }
        }
    }

    private fun onOrbTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (speech.isListening.value) {
                speech.stopListening()
            } else {
                speech.startListening { handleCommand(it) }
            }
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun handleCommand(text: String) {
        lastCommand = text
        // Hier später: Text an Hermes' Kommando-Logik / Backend übergeben
    }

    override fun onDestroy() {
        speech.destroy()
        super.onDestroy()
    }
}

@Composable
fun HermesScreen(
    isListening: Boolean,
    partialText: String,
    finalText: String,
    rmsLevel: Float,
    onOrbTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HERMES",
            color = Color(0xFF00E5FF),
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 8.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable { onOrbTap() }
        ) {
            JarvisOrb(isListening = isListening, rmsLevel = rmsLevel)
            Text(
                text = if (isListening) "HÖRT ZU..." else "TIPPEN\nZUM SPRECHEN",
                color = Color(0xFF00E5FF),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = when {
                isListening && partialText.isNotBlank() -> partialText
                finalText.isNotBlank() -> "> $finalText"
                else -> ""
            },
            color = Color(0xFF80DEEA),
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }
}

package com.hermes.assistant.ui

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val HermesCyan = Color(0xFF00E5FF)
private val HermesDeepBlue = Color(0xFF012A4A)

/**
 * Jarvis-artiger Energie-Ring:
 * - dreht sich permanent langsam (Idle-Zustand)
 * - pulsiert stärker und schneller, wenn zugehört wird (rmsLevel steigt)
 * - zeigt einen zweiten, gegenläufigen Ring bei aktiver Spracherkennung
 */
@Composable
fun JarvisOrb(
    isListening: Boolean,
    rmsLevel: Float, // 0f..10f, aus SpeechRecognitionManager
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "jarvis")

    val baseRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = { it }), RepeatMode.Restart),
        label = "baseRotation"
    )
    val counterRotation by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(4000, easing = { it }), RepeatMode.Restart),
        label = "counterRotation"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = modifier.size(260.dp)) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2.4f
            val amplitude = (rmsLevel / 10f).coerceIn(0f, 1f)

            // Äußerer Ring: reagiert auf Lautstärke, wird bei Sprache heller/dicker
            drawCircle(
                color = HermesCyan.copy(alpha = 0.25f + amplitude * 0.5f),
                radius = baseRadius * pulse * (1f + amplitude * 0.15f),
                center = center,
                style = Stroke(width = 3.dp.toPx() + amplitude * 6.dp.toPx())
            )

            // Innerer, statischer HUD-Ring
            drawCircle(
                color = HermesDeepBlue,
                radius = baseRadius * 0.75f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotierende Tick-Marken (Idle-Deko, immer sichtbar)
            rotate(baseRotation, pivot = center) {
                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30).toDouble())
                    val r1 = baseRadius * 0.85f
                    val r2 = baseRadius * 0.95f
                    val start = Offset(
                        center.x + (r1 * cos(angle)).toFloat(),
                        center.y + (r1 * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        center.x + (r2 * cos(angle)).toFloat(),
                        center.y + (r2 * sin(angle)).toFloat()
                    )
                    drawLine(HermesCyan.copy(alpha = 0.6f), start, end, strokeWidth = 2.dp.toPx())
                }
            }

            // Zweiter, gegenläufiger Ring – nur aktiv während Spracherkennung läuft
            if (isListening) {
                rotate(counterRotation, pivot = center) {
                    drawCircle(
                        color = HermesCyan.copy(alpha = 0.4f),
                        radius = baseRadius * 1.15f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
    }
}

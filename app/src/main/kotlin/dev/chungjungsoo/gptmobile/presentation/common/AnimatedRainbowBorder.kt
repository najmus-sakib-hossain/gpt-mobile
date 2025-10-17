package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.dp
import kotlin.math.PI

/**
 * Animated Rainbow Border with Blur/Glow Effect
 * Wraps content with a customizable animated rainbow border with CSS-like blur glow
 */
@Composable
fun AnimatedRainbowBorder(
    modifier: Modifier = Modifier,
    borderRadius: Float = 32f,
    borderWidth: Float = 4f,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    // Infinite animation for rainbow effect
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_progress"
    )
    
    // Pulsing glow animation
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(modifier = modifier) {
        // Content
        content()

        // Border overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = borderWidth.dp.toPx()
            val radius = borderRadius.dp.toPx()

            // Rainbow colors
            val colors = listOf(
                Color(0xFFFF0000), // Red
                Color(0xFFFF7F00), // Orange
                Color(0xFFFFFF00), // Yellow
                Color(0xFF00FF00), // Green
                Color(0xFF0000FF), // Blue
                Color(0xFF4B0082), // Indigo
                Color(0xFF9400D3), // Violet
                Color(0xFFFF0000)  // Red again for smooth loop
            )

            // Create gradient brush that rotates
            val brush = Brush.sweepGradient(
                colors = colors,
                center = Offset(width / 2, height / 2)
            )

            // Draw multiple layers for blur/glow effect (CSS-like blur simulation)
            val glowLayers = 5
            for (i in glowLayers downTo 1) {
                val layerAlpha = (0.15f / i) * glowPulse
                val layerStrokeWidth = strokeWidth + (i * 2f)
                
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(
                        (strokeWidth - layerStrokeWidth) / 2 + layerStrokeWidth / 2,
                        (strokeWidth - layerStrokeWidth) / 2 + layerStrokeWidth / 2
                    ),
                    size = Size(
                        width - layerStrokeWidth,
                        height - layerStrokeWidth
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(
                        width = layerStrokeWidth,
                        cap = StrokeCap.Round
                    ),
                    alpha = layerAlpha,
                    blendMode = BlendMode.Plus
                )
            }

            // Draw the main sharp rainbow border
            drawRoundRect(
                brush = brush,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                alpha = 1.0f
            )
            
            // Additional bright glow layer for extra luminosity
            drawRoundRect(
                brush = brush,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(
                    width = strokeWidth * 0.5f,
                    cap = StrokeCap.Round
                ),
                alpha = 0.6f * glowPulse,
                blendMode = BlendMode.Plus
            )
        }
    }
}

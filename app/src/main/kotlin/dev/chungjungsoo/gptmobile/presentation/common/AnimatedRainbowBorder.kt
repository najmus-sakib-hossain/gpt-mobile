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
    borderRadius: Float = 50f,
    borderWidth: Float = 12f,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }
    
    // Debug logging
    println("🌈 AnimatedRainbowBorder recomposing - radius: $borderRadius, width: $borderWidth")

    // Infinite animation for rainbow rotation - OUTSIDE key() to maintain continuity
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_rotation"
    )
    
    // Pulsing glow animation for intensity
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    
    // Shadow spread animation
    val shadowSpread by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_spread"
    )

    Box(modifier = modifier) {
        // Content
        content()

        // Border overlay with CSS-like filter effects
        // Use Canvas for better invalidation and parameter tracking
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            println("🎨 Canvas drawing - radius: $borderRadius, width: $borderWidth, animProgress: $animationProgress")
            
            val width = size.width
            val height = size.height
            val strokeWidth = borderWidth.dp.toPx()
            val radius = borderRadius.dp.toPx()
            
            // Capture animation values - IMPORTANT: These must be referenced here to trigger redraws
            val animProgress = animationProgress
            val pulse = glowPulse
            val spread = shadowSpread
            
            println("✏️ Calculated - strokeWidth: $strokeWidth, radius: $radius")
            
            // Rainbow colors
            val baseColors = listOf(
                Color(0xFFFF0000), // Red
                Color(0xFFFF7F00), // Orange
                Color(0xFFFFFF00), // Yellow
                Color(0xFF00FF00), // Green
                Color(0xFF0000FF), // Blue
                Color(0xFF4B0082), // Indigo
                Color(0xFF9400D3), // Violet
                Color(0xFFFF0000)  // Red again for smooth loop
            )

            // Calculate rotation angle in radians
            val angleRad = animProgress * (PI / 180f).toFloat()
            
            // Create rotating gradient brush with smooth color interpolation
            val centerX = width / 2
            val centerY = height / 2
            
            // Generate colors for sweep gradient with rotation
            val rotatedColors = mutableListOf<Color>()
            val numStops = 8
            for (i in 0 until numStops) {
                val angle = (i.toFloat() / numStops * 360f + animProgress) % 360f
                val colorIndex = (angle / 360f * baseColors.size).toInt() % baseColors.size
                val nextIndex = (colorIndex + 1) % baseColors.size
                val fraction = (angle / 360f * baseColors.size) % 1f
                
                // Interpolate between colors for smooth transition
                val color = Color(
                    red = baseColors[colorIndex].red * (1 - fraction) + baseColors[nextIndex].red * fraction,
                    green = baseColors[colorIndex].green * (1 - fraction) + baseColors[nextIndex].green * fraction,
                    blue = baseColors[colorIndex].blue * (1 - fraction) + baseColors[nextIndex].blue * fraction,
                    alpha = 1f
                )
                rotatedColors.add(color)
            }
            rotatedColors.add(rotatedColors[0]) // Close the loop
            
            val brush = Brush.sweepGradient(
                colors = rotatedColors,
                center = Offset(centerX, centerY)
            )
            
            // LAYER 1: Outer Shadow (CSS drop-shadow) - Optimized
            // Simulates: filter: drop-shadow(0 0 20px rgba(rainbow, 0.6))
            // ============================================
            val shadowLayers = 3  // Further reduced for better performance
            for (i in shadowLayers downTo 1) {
                val shadowOffset = i * 4f * spread
                val shadowAlpha = (0.12f / i) * pulse
                val shadowStrokeWidth = strokeWidth + shadowOffset
                
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(
                        shadowStrokeWidth / 2,
                        shadowStrokeWidth / 2
                    ),
                    size = Size(
                        width - shadowStrokeWidth,
                        height - shadowStrokeWidth
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(
                        width = shadowStrokeWidth / 2,
                        cap = StrokeCap.Round
                    ),
                    alpha = shadowAlpha,
                    blendMode = BlendMode.Plus
                )
            }

            // ============================================
            // LAYER 2: Blur Effect (CSS blur) - Optimized
            // Simulates: filter: blur(8px)
            // ============================================
            val blurLayers = 2  // Further reduced for better performance
            for (i in blurLayers downTo 1) {
                val blurOffset = i * 2.5f
                val blurAlpha = (0.15f / i) * pulse
                val blurStrokeWidth = strokeWidth + blurOffset
                
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(
                        strokeWidth / 2,
                        strokeWidth / 2
                    ),
                    size = Size(
                        width - strokeWidth,
                        height - strokeWidth
                    ),
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(
                        width = blurStrokeWidth,
                        cap = StrokeCap.Round
                    ),
                    alpha = blurAlpha,
                    blendMode = BlendMode.Plus
                )
            }

            // ============================================
            // LAYER 3: Main Border (Sharp, crisp edge)
            // ============================================
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
            
            // ============================================
            // LAYER 4: Highlight Glow (Bright edge)
            // Simulates: filter: brightness(1.2) saturate(1.3)
            // ============================================
            drawRoundRect(
                brush = brush,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(width - strokeWidth, height - strokeWidth),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                alpha = 0.6f * pulse,
                blendMode = BlendMode.Plus
            )
            
            // ============================================
            // LAYER 5: Ultra Bright Core
            // Creates the "neon" effect center
            // ============================================
            // drawRoundRect(
            //     brush = brush,
            //     topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            //     size = Size(width - strokeWidth, height - strokeWidth),
            //     cornerRadius = CornerRadius(radius, radius),
            //     style = Stroke(
            //         width = strokeWidth * 0.2f,
            //         cap = StrokeCap.Round
            //     ),
            //     alpha = 0.7f * pulse,
            //     blendMode = BlendMode.Screen
            // )
        }  // End Canvas
    }  // End Box
}
package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// Dynamic rainbow palette builder
private fun buildRainbowGlowColors(steps: Int, saturation: Float): List<Color> = buildList {
    for (i in 0..steps) { // include last == first to ensure seamless tiling
        val hue = i * (360f / steps)
        add(colorFromHsv(hue, s = saturation, v = 1f))
    }
}

private fun colorFromHsv(h: Float, s: Float, v: Float): Color {
    val hh = ((h % 360f) + 360f) % 360f
    val c = v * s
    val x = c * (1 - abs((hh / 60f) % 2 - 1))
    val m = v - c
    val (r1, g1, b1) = when {
        hh < 60f -> Triple(c, x, 0f)
        hh < 120f -> Triple(x, c, 0f)
        hh < 180f -> Triple(0f, c, x)
        hh < 240f -> Triple(0f, x, c)
        hh < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m, 1f)
}

@Composable
fun GeneratingSkeleton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderWidth: Dp = 8.dp, // not used here, but keeping for API compatibility
    rotationDurationMillis: Int = 5000, // faster animation
    shimmerDurationMillis: Int = 3000, // faster shimmer
    contentPadding: Dp = 16.dp,
    colorSteps: Int = 60, // customizable color steps
    cycleMultiplier: Float = 3f, // customizable cycle width
    saturation: Float = 0.80f, // customizable saturation
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "generating-skeleton-transition")

    val colorShiftFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow-color-shift"
    )

    val shimmerFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shimmerDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow-shimmer"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawRainbowGlow(
                cornerRadius = cornerRadius,
                colorShiftFraction = colorShiftFraction,
                shimmerFraction = shimmerFraction,
                colorSteps = colorSteps,
                cycleMultiplier = cycleMultiplier,
                saturation = saturation
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

private fun Modifier.drawRainbowGlow(
    cornerRadius: Dp,
    colorShiftFraction: Float,
    shimmerFraction: Float,
    colorSteps: Int,
    cycleMultiplier: Float,
    saturation: Float
): Modifier = this.then(
    Modifier.drawBehind {
        val radiusPx = cornerRadius.toPx()

        // Build dynamic rainbow colors based on parameters
        val rainbowColors = buildRainbowGlowColors(colorSteps, saturation)

        // Wider cycle for ultra-smooth color blending
        val cycleWidth = size.width.coerceAtLeast(1f) * cycleMultiplier

        // Move left to right: phase increases positively
        val phase = colorShiftFraction * cycleWidth
        val startX = phase
        val endX = startX + cycleWidth

        // Ultra-smooth rainbow with seamless wrapping and many color stops
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = rainbowColors,
                startX = startX,
                endX = endX,
                tileMode = TileMode.Repeated
            ),
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            size = size
        )

        // Shimmer: moves left to right with smooth blending
        val shimmerWidth = size.width * 0.6f
        val shimmerStart = shimmerFraction * (size.width + shimmerWidth) - shimmerWidth
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0f)
                ),
                startX = shimmerStart,
                endX = shimmerStart + shimmerWidth,
                tileMode = TileMode.Clamp
            ),
            size = size,
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )
    }
)

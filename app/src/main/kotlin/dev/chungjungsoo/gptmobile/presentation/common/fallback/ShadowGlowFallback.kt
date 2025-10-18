package dev.chungjungsoo.gptmobile.presentation.common.fallback

import android.graphics.BlurMaskFilter
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidTileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom shadow glow implementation with proper gradient and blur support.
 * Based on ShadowGlow library but simplified for our use case.
 */

enum class ShadowBlurStyle {
    NORMAL, SOLID, OUTER, INNER
}

internal fun ShadowBlurStyle.toAndroidBlurStyle(): BlurMaskFilter.Blur {
    return when (this) {
        ShadowBlurStyle.NORMAL -> BlurMaskFilter.Blur.NORMAL
        ShadowBlurStyle.SOLID -> BlurMaskFilter.Blur.SOLID
        ShadowBlurStyle.OUTER -> BlurMaskFilter.Blur.OUTER
        ShadowBlurStyle.INNER -> BlurMaskFilter.Blur.INNER
    }
}

@Composable
private fun rememberAnimatedBreathingValue(
    enabled: Boolean,
    intensity: Dp,
    durationMillis: Int
): State<Float> {
    val density = LocalDensity.current
    val intensityPx = remember(intensity) { with(density) { intensity.toPx() } }

    if (!enabled || intensityPx <= 0f || durationMillis <= 0) {
        return remember { mutableFloatStateOf(0f) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathingEffect")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = intensityPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingValue"
    )
}

/**
 * Applies a drop shadow effect with solid color.
 */
fun Modifier.shadowGlow(
    color: Color = Color.Black.copy(alpha = 0.4f),
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp,
    enableBreathingEffect: Boolean = false,
    breathingEffectIntensity: Dp = 4.dp,
    breathingDurationMillis: Int = 1500
): Modifier = composed {
    val animatedBreathingValuePx by rememberAnimatedBreathingValue(
        enabled = enableBreathingEffect,
        intensity = breathingEffectIntensity,
        durationMillis = breathingDurationMillis
    )

    this.then(
        Modifier.drawBehind {
            val spreadPx = spread.toPx()
            val baseBlurRadiusPx = blurRadius.toPx()
            val totalBlurRadiusPx = (baseBlurRadiusPx + animatedBreathingValuePx).coerceAtLeast(0f)

            val totalOffsetXPx = offsetX.toPx()
            val totalOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            val shadowColorArgb = color.toArgb()

            if (color.alpha == 0f && totalBlurRadiusPx <= 0f && spreadPx == 0f) {
                return@drawBehind
            }

            val frameworkPaint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                this.color = shadowColorArgb
                if (totalBlurRadiusPx > 0f) {
                    maskFilter = BlurMaskFilter(totalBlurRadiusPx, blurStyle.toAndroidBlurStyle())
                }
            }

            val left = -spreadPx + totalOffsetXPx
            val top = -spreadPx + totalOffsetYPx
            val right = size.width + spreadPx + totalOffsetXPx
            val bottom = size.height + spreadPx + totalOffsetYPx

            drawShadowShape(left, top, right, bottom, shadowBorderRadiusPx, frameworkPaint)
        }
    )
}

/**
 * Applies a drop shadow effect with gradient colors.
 * Enhanced with multi-layer glow for intense, vibrant rainbow effect.
 */
fun Modifier.shadowGlow(
    gradientColors: List<Color>,
    gradientStartFactorX: Float = 0f,
    gradientStartFactorY: Float = 0f,
    gradientEndFactorX: Float = 1f,
    gradientEndFactorY: Float = 1f,
    gradientColorStops: List<Float>? = null,
    gradientTileMode: TileMode = TileMode.Clamp,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    alpha: Float = 1.0f,
    blurStyle: ShadowBlurStyle = ShadowBlurStyle.NORMAL,
    enableGyroParallax: Boolean = false,
    parallaxSensitivity: Dp = 4.dp,
    enableBreathingEffect: Boolean = false,
    breathingEffectIntensity: Dp = 4.dp,
    breathingDurationMillis: Int = 1500,
    glowLayers: Int = 5  // NEW: Number of glow layers for intensity
): Modifier = composed {
    val animatedBreathingValuePx by rememberAnimatedBreathingValue(
        enabled = enableBreathingEffect,
        intensity = breathingEffectIntensity,
        durationMillis = breathingDurationMillis
    )

    this.then(
        Modifier.drawBehind {
            if (gradientColors.isEmpty() || alpha == 0f) {
                return@drawBehind
            }

            val spreadPx = spread.toPx()
            val baseBlurRadiusPx = blurRadius.toPx()
            val totalBlurRadiusPx = (baseBlurRadiusPx + animatedBreathingValuePx).coerceAtLeast(0f)

            val totalOffsetXPx = offsetX.toPx()
            val totalOffsetYPx = offsetY.toPx()
            val shadowBorderRadiusPx = borderRadius.toPx()

            // Calculate gradient coordinates based on the shadow bounds
            val actualStartX = gradientStartFactorX * size.width
            val actualStartY = gradientStartFactorY * size.height
            val actualEndX = gradientEndFactorX * size.width
            val actualEndY = gradientEndFactorY * size.height

            // Draw multiple glow layers for intense effect
            for (layer in glowLayers downTo 1) {
                val layerScale = layer.toFloat() / glowLayers
                val layerBlur = totalBlurRadiusPx * (0.5f + layerScale * 0.5f)
                val layerSpread = spreadPx * layerScale
                val layerAlpha = (alpha * (0.4f + layerScale * 0.3f)).coerceIn(0f, 1f)

                // Create fresh Paint for each layer
                val frameworkPaint = AndroidPaint().apply {
                    isAntiAlias = true
                    style = AndroidPaint.Style.FILL
                    this.alpha = (layerAlpha * 255).toInt()
                    
                    // Use Android's LinearGradient for proper rainbow rendering
                    shader = AndroidLinearGradient(
                        actualStartX, actualStartY,
                        actualEndX, actualEndY,
                        gradientColors.map { it.toArgb() }.toIntArray(),
                        gradientColorStops?.toFloatArray(),
                        gradientTileMode.toAndroidTileMode()
                    )
                    
                    // Apply blur after setting shader
                    if (layerBlur > 0f) {
                        maskFilter = BlurMaskFilter(layerBlur, blurStyle.toAndroidBlurStyle())
                    }
                }

                val left = -layerSpread + totalOffsetXPx
                val top = -layerSpread + totalOffsetYPx
                val right = size.width + layerSpread + totalOffsetXPx
                val bottom = size.height + layerSpread + totalOffsetYPx

                drawShadowShape(left, top, right, bottom, shadowBorderRadiusPx, frameworkPaint)
            }
        }
    )
}

private fun DrawScope.drawShadowShape(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    cornerRadiusPx: Float,
    paint: AndroidPaint
) {
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            left, top, right, bottom,
            cornerRadiusPx, cornerRadiusPx,
            paint
        )
    }
}

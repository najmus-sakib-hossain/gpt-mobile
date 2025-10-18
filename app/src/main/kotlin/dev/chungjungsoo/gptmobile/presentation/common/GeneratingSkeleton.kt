package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chungjungsoo.gptmobile.data.dto.GlowAnimationStyle
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

// Safe easing - use FastOutSlowInEasing which is guaranteed stable
private val SmoothGrowEasing = androidx.compose.animation.core.FastOutSlowInEasing

// Simple custom easing with slight overshoot - guaranteed to work
private fun easeOutBack(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val adjusted = t - 1f
    return 1f + (c1 + 1f) * adjusted * adjusted * adjusted + c1 * adjusted * adjusted
}

@Composable
fun GeneratingSkeleton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 8.dp, // not used here, but keeping for API compatibility
    rotationDurationMillis: Int = 5000,
    shimmerDurationMillis: Int = 6000,
    contentPadding: Dp = 16.dp,
    colorSteps: Int = 120,
    cycleMultiplier: Float = 5.0f,
    saturation: Float = 1.00f,
    animationStyle: GlowAnimationStyle = GlowAnimationStyle.TOP_CENTER_GROW,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val isGrowStyle = animationStyle != GlowAnimationStyle.CONTINUOUS_FLOW
    val growAnim = remember(animationStyle) { Animatable(if (isGrowStyle) 0f else 1f) }
    var growComplete by remember(animationStyle) { mutableStateOf(!isGrowStyle) }
    val latestAnimationStyle by rememberUpdatedState(animationStyle)
    
    val growSpec: AnimationSpec<Float> = remember(animationStyle) {
        tween(
            durationMillis = 1800,
            easing = SmoothGrowEasing
        )
    }
    
    LaunchedEffect(animationStyle) {
        if (isGrowStyle) {
            growComplete = false
            growAnim.snapTo(0f)
            growAnim.animateTo(targetValue = 1f, animationSpec = growSpec)
            growComplete = true
        } else {
            growAnim.snapTo(1f)
            growComplete = true
        }
    }
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

    val growFraction = if (isGrowStyle) easeOutBack(growAnim.value) else 1f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawRainbowGlow(
                cornerRadius = cornerRadius,
                colorShiftFraction = colorShiftFraction,
                shimmerFraction = shimmerFraction,
                colorSteps = colorSteps,
                cycleMultiplier = cycleMultiplier,
                saturation = saturation,
                animationStyle = animationStyle,
                growFraction = growFraction,
                isGrowPhase = isGrowStyle && !growComplete
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
    saturation: Float,
    animationStyle: GlowAnimationStyle,
    growFraction: Float,
    isGrowPhase: Boolean
): Modifier = this.then(
    Modifier.drawBehind {
        val radiusPx = cornerRadius.toPx()
        val rainbowColors = buildRainbowGlowColors(colorSteps, saturation)

        // ALWAYS draw the same continuous flow rainbow animation
        val cycleWidth = size.width.coerceAtLeast(1f) * cycleMultiplier
        val phase = colorShiftFraction * cycleWidth
        val startX = phase
        val endX = startX + cycleWidth

        val rainbowBrush = Brush.horizontalGradient(
            colors = rainbowColors,
            startX = startX,
            endX = endX,
            tileMode = TileMode.Repeated
        )

        // Apply clipping/masking based on animation style
        if (animationStyle == GlowAnimationStyle.CONTINUOUS_FLOW) {
            // No mask - show full rainbow
            drawRoundRect(
                brush = rainbowBrush,
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                size = size
            )
        } else {
            // Mask the rainbow to create grow effect
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            
            // Create a clip path based on the animation style
            clipPath(
                path = Path().apply {
                    when (animationStyle) {
                        GlowAnimationStyle.BOTTOM_CENTER_GROW -> {
                            val clipHeight = size.height * growFraction
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = 0f,
                                    top = size.height - clipHeight,
                                    right = size.width,
                                    bottom = size.height,
                                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                                )
                            )
                        }
                        GlowAnimationStyle.TOP_CENTER_GROW -> {
                            val clipHeight = size.height * growFraction
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = 0f,
                                    top = 0f,
                                    right = size.width,
                                    bottom = clipHeight,
                                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                                )
                            )
                        }
                        GlowAnimationStyle.LEFT_CENTER_GROW -> {
                            val clipWidth = size.width * growFraction
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = 0f,
                                    top = 0f,
                                    right = clipWidth,
                                    bottom = size.height,
                                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                                )
                            )
                        }
                        GlowAnimationStyle.RIGHT_CENTER_GROW -> {
                            val clipWidth = size.width * growFraction
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = size.width - clipWidth,
                                    top = 0f,
                                    right = size.width,
                                    bottom = size.height,
                                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                                )
                            )
                        }
                        GlowAnimationStyle.CENTER_EXPAND -> {
                            val clipWidth = size.width * growFraction
                            val clipHeight = size.height * growFraction
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    left = centerX - clipWidth / 2f,
                                    top = centerY - clipHeight / 2f,
                                    right = centerX + clipWidth / 2f,
                                    bottom = centerY + clipHeight / 2f,
                                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                                )
                            )
                        }
                        else -> {}
                    }
                }
            ) {
                // Draw the rainbow inside the clipped region
                drawRoundRect(
                    brush = rainbowBrush,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    size = size
                )
            }
        }

        // Add shimmer on top (always, but more subtle during grow)
        val shimmerAlpha = if (isGrowPhase) 0.5f else 1f
        val shimmerWidth = size.width * 0.6f
        val shimmerStart = shimmerFraction * (size.width + shimmerWidth) - shimmerWidth
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0f * shimmerAlpha),
                    Color.White.copy(alpha = 0.12f * shimmerAlpha),
                    Color.White.copy(alpha = 0.25f * shimmerAlpha),
                    Color.White.copy(alpha = 0.12f * shimmerAlpha),
                    Color.White.copy(alpha = 0f * shimmerAlpha)
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

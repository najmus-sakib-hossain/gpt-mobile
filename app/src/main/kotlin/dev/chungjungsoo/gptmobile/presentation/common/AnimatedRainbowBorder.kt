package dev.chungjungsoo.gptmobile.presentation.common

import android.graphics.Matrix
import android.graphics.PathMeasure
import android.graphics.SweepGradient
import android.graphics.Path as AndroidPath
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.chungjungsoo.gptmobile.data.dto.RainbowAnimationStyle

private val RainbowColors: List<Color> = listOf(
    Color(0xFFFF0000),
    Color(0xFFFF7F00),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF0000FF),
    Color(0xFF4B0082),
    Color(0xFF9400D3),
    Color(0xFFFF0000)
)

private val RainbowBaseStops: FloatArray = run {
    val step = if (RainbowColors.size > 1) 1f / (RainbowColors.size - 1) else 1f
    FloatArray(RainbowColors.size) { index -> index * step }
}

private val RainbowColorInts: IntArray = IntArray(RainbowColors.size) { index ->
    RainbowColors[index].toArgb()
}

private val SmoothRevealEasing = CubicBezierEasing(0.38f, 0f, 0.22f, 1f)

private fun easeOutBack(value: Float, overshoot: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    val c1 = overshoot
    val c3 = c1 + 1f
    val t = clamped - 1f
    return 1f + c3 * t * t * t + c1 * t * t
}

private data class BorderGeometry(
    val strokeWidthPx: Float,
    val radiusPx: Float,
    val rect: Rect,
    val outline: Path,
    val pathMeasure: PathMeasure,
    val totalLength: Float,
    val topRightAnchor: Float,
    val bottomCenterAnchor: Float,
    val center: Offset,
    val shader: SweepGradient
)

private fun buildBorderGeometry(
    layoutSize: IntSize,
    density: Density,
    borderWidth: Float,
    borderRadius: Float
): BorderGeometry? {
    if (layoutSize.width <= 0 || layoutSize.height <= 0) return null

    val widthPx = layoutSize.width.toFloat()
    val heightPx = layoutSize.height.toFloat()
    if (widthPx <= 0f || heightPx <= 0f) return null

    val strokeWidthPx = with(density) { borderWidth.dp.toPx() }
    val radiusPx = with(density) { borderRadius.dp.toPx() }
    val inset = strokeWidthPx / 2f
    val rect = Rect(
        offset = Offset(inset, inset),
        size = Size(widthPx - strokeWidthPx, heightPx - strokeWidthPx)
    )
    if (rect.width <= 0f || rect.height <= 0f) return null

    val outline = Path().apply {
        addRoundRect(RoundRect(rect, CornerRadius(radiusPx, radiusPx)))
    }
    val androidOutline = outline.asAndroidPath()
    val pathMeasure = PathMeasure(androidOutline, true)
    val totalLength = pathMeasure.length
    if (totalLength <= 0f) return null

    fun findOffsetFor(target: Offset): Float {
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        var bestDistance = Float.MAX_VALUE
        var bestOffset = 0f
        val steps = 256
        for (i in 0..steps) {
            val distance = totalLength * i / steps
            if (pathMeasure.getPosTan(distance, pos, tan)) {
                val dx = pos[0] - target.x
                val dy = pos[1] - target.y
                val candidate = dx * dx + dy * dy
                if (candidate < bestDistance) {
                    bestDistance = candidate
                    bestOffset = distance
                }
            }
        }
        return bestOffset
    }

    val topRightAnchor = findOffsetFor(
        Offset(
            x = rect.right - radiusPx * 0.35f,
            y = rect.top + radiusPx * 0.65f
        )
    )
    val bottomCenterAnchor = findOffsetFor(
        Offset(
            x = rect.left + rect.width / 2f,
            y = rect.bottom - radiusPx * 0.25f
        )
    )

    val center = rect.center
    val shader = SweepGradient(center.x, center.y, RainbowColorInts, RainbowBaseStops)

    return BorderGeometry(
        strokeWidthPx = strokeWidthPx,
        radiusPx = radiusPx,
        rect = rect,
        outline = outline,
        pathMeasure = pathMeasure,
        totalLength = totalLength,
        topRightAnchor = topRightAnchor,
        bottomCenterAnchor = bottomCenterAnchor,
        center = center,
        shader = shader
    )
}

private fun shapedRevealFraction(style: RainbowAnimationStyle, fraction: Float): Float = when (style) {
    RainbowAnimationStyle.TOP_RIGHT_BOUNCE -> SmoothRevealEasing.transform(fraction.coerceIn(0f, 1f))
    RainbowAnimationStyle.BOTTOM_CENTER_REVEAL -> easeOutBack(fraction, overshoot = 1.06f)
    else -> fraction.coerceIn(0f, 1f)
}

private fun forwardBias(style: RainbowAnimationStyle): Float = when (style) {
    RainbowAnimationStyle.TOP_RIGHT_BOUNCE -> 0.55f
    RainbowAnimationStyle.BOTTOM_CENTER_REVEAL -> 0.57f
    else -> 0.5f
}

@Composable
fun AnimatedRainbowBorder(
    modifier: Modifier = Modifier,
    borderRadius: Float = 50f,
    borderWidth: Float = 12f,
    enabled: Boolean = true,
    animationStyle: RainbowAnimationStyle = RainbowAnimationStyle.CONTINUOUS_SWEEP,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier) { content() }
        return
    }

    val revealStyleActive = animationStyle != RainbowAnimationStyle.CONTINUOUS_SWEEP
    val revealAnim = remember(animationStyle) { Animatable(if (revealStyleActive) 0f else 1f) }
    var revealComplete by remember(animationStyle) { mutableStateOf(!revealStyleActive) }
    val latestEnabled by rememberUpdatedState(enabled)
    val revealSpec: AnimationSpec<Float> = remember(animationStyle) {
        when (animationStyle) {
            RainbowAnimationStyle.TOP_RIGHT_BOUNCE -> tween(
                durationMillis = 5000,
                easing = SmoothRevealEasing
            )

            RainbowAnimationStyle.BOTTOM_CENTER_REVEAL -> tween(
                durationMillis = 5000,
                easing = SmoothRevealEasing
            )

            else -> tween(
                durationMillis = 520,
                easing = CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
            )
        }
    }

    LaunchedEffect(animationStyle, latestEnabled) {
        if (!latestEnabled) {
            revealComplete = !revealStyleActive
            revealAnim.snapTo(if (revealStyleActive) 0f else 1f)
            return@LaunchedEffect
        }

        if (revealStyleActive) {
            revealComplete = false
            revealAnim.snapTo(0f)
            revealAnim.animateTo(targetValue = 1f, animationSpec = revealSpec)
            revealComplete = true
        } else {
            revealAnim.snapTo(1f)
            revealComplete = true
        }
    }

    var layoutSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val densityKey = density.density to density.fontScale
    val geometry = remember(layoutSize, borderWidth, borderRadius, densityKey) {
        buildBorderGeometry(layoutSize, density, borderWidth, borderRadius)
    }
    val gradientPaint = remember { Paint() }
    val gradientMatrix = remember { Matrix() }

    val infiniteTransition = rememberInfiniteTransition(label = "rainbow-border")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow-rotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rainbow-glow"
    )

    val revealFractionRaw = if (revealStyleActive) revealAnim.value else 1f
    val revealPhaseActive = revealStyleActive && !revealComplete
    val revealFractionShaped = if (revealPhaseActive) {
        shapedRevealFraction(animationStyle, revealFractionRaw).coerceIn(0f, 1.12f)
    } else {
        1f
    }

    Box(modifier = modifier.onSizeChanged { layoutSize = it }) {
        content()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val geometryState = geometry ?: return@Canvas
            val strokeWidthPx = geometryState.strokeWidthPx
            val radiusPx = geometryState.radiusPx
            val rect = geometryState.rect
            val outline = geometryState.outline
            val pathMeasure = geometryState.pathMeasure
            val totalLength = geometryState.totalLength
            val topRightAnchor = geometryState.topRightAnchor
            val bottomCenterAnchor = geometryState.bottomCenterAnchor

            fun appendSegment(target: AndroidPath, start: Float, end: Float) {
                var segStart = start
                var segEnd = end
                var moveTo = target.isEmpty

                while (segStart < 0f) {
                    segStart += totalLength
                    segEnd += totalLength
                }
                while (segStart >= totalLength) {
                    segStart -= totalLength
                    segEnd -= totalLength
                }

                if (segEnd <= segStart) return

                if (segEnd <= totalLength) {
                    pathMeasure.getSegment(segStart, segEnd, target, moveTo)
                } else {
                    pathMeasure.getSegment(segStart, totalLength, target, moveTo)
                    moveTo = false
                    pathMeasure.getSegment(0f, segEnd - totalLength, target, moveTo)
                }
            }

            val revealPath: Path? = if (revealPhaseActive) {
                val fraction = revealFractionShaped
                if (fraction <= 0f) {
                    null
                } else {
                    val anchor = when (animationStyle) {
                        RainbowAnimationStyle.TOP_RIGHT_BOUNCE -> topRightAnchor
                        RainbowAnimationStyle.BOTTOM_CENTER_REVEAL -> bottomCenterAnchor
                        else -> topRightAnchor
                    }
                    val visibleLength = totalLength * fraction
                    val forwardPortion = forwardBias(animationStyle).coerceIn(0.35f, 0.85f)
                    val forwardVisible = visibleLength * forwardPortion
                    val backwardVisible = visibleLength - forwardVisible
                    val segmentPath = AndroidPath()
                    appendSegment(segmentPath, anchor - backwardVisible, anchor)
                    appendSegment(segmentPath, anchor, anchor + forwardVisible)
                    if (segmentPath.isEmpty) null else segmentPath.asComposePath()
                }
            } else null

            val activePath = revealPath ?: outline

            val glowAlpha = 0.18f * glowPulse * if (revealPhaseActive) 0.7f else 1f
            if (glowAlpha > 0f) {
                drawPath(
                    path = activePath,
                    color = Color.White.copy(alpha = glowAlpha),
                    style = Stroke(
                        width = strokeWidthPx + strokeWidthPx * 0.45f,
                        cap = StrokeCap.Round
                    )
                )
            }

            if (revealPhaseActive) {
                val anchor = when (animationStyle) {
                    RainbowAnimationStyle.TOP_RIGHT_BOUNCE -> topRightAnchor
                    RainbowAnimationStyle.BOTTOM_CENTER_REVEAL -> bottomCenterAnchor
                    else -> null
                }
                val anchorPos = FloatArray(2)
                if (anchor != null && pathMeasure.getPosTan(anchor, anchorPos, null)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.22f * glowPulse),
                        radius = strokeWidthPx * 1.35f * revealFractionShaped.coerceIn(0.6f, 1.1f),
                        center = Offset(anchorPos[0], anchorPos[1])
                    )
                }
            }

            val sweepShader = geometryState.shader
            gradientMatrix.reset()
            gradientMatrix.postRotate(rotation, geometryState.center.x, geometryState.center.y)
            sweepShader.setLocalMatrix(gradientMatrix)

            drawIntoCanvas { canvas ->
                val paint = gradientPaint
                paint.style = PaintingStyle.Stroke
                paint.strokeWidth = strokeWidthPx
                paint.strokeCap = StrokeCap.Round
                paint.asFrameworkPaint().apply {
                    isAntiAlias = true
                    setShader(sweepShader)
                }
                canvas.drawPath(activePath, paint)
            }

            val highlightAlpha = 0.08f * glowPulse * if (revealPhaseActive) 0.6f else 1f
            if (highlightAlpha > 0f) {
                drawPath(
                    path = activePath,
                    color = Color.White.copy(alpha = highlightAlpha),
                    style = Stroke(
                        width = strokeWidthPx * 0.55f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

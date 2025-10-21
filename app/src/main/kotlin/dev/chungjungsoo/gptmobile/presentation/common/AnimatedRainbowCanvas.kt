
package dev.chungjungsoo.gptmobile.presentation.common

import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.PathMeasure
import android.graphics.SweepGradient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Data class to hold information about each drawn shape
private data class DrawingShape(
    val path: Path,
    val isCircle: Boolean,
    // Animatable for the fade-out effect - used for white glow fade out
    val animation: Animatable<Float, *> = Animatable(0f),
    // For rainbow effect, we need a center and a pre-created shader
    val center: Offset,
    val shader: SweepGradient,
    // Track if this is a completed shape (no longer being drawn)
    val isCompleted: Boolean = false
)

// Using a dedicated data class for glow layers avoids type inference issues.
private data class GlowLayer(val width: Float, val alpha: Float)

// Rainbow colors for the glowing effect
private val RainbowColors: List<Color> = listOf(
    Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00), Color(0xFF00FF00),
    Color(0xFF0000FF), Color(0xFF4B0082), Color(0xFF9400D3), Color(0xFFFF0000)
)
private val RainbowColorInts: IntArray = RainbowColors.map { it.toArgb() }.toIntArray()
private val RainbowStops: FloatArray = FloatArray(RainbowColors.size) { i -> i.toFloat() / (RainbowColors.size - 1) }

/**
 * A full-screen canvas that allows drawing with touch.
 * - While drawing: shows animated rainbow glow effect
 * - When touch ends (circle): triggers white fading glow animation and then disappears
 * - When touch ends (other shapes): triggers white fading glow animation and then disappears
 */
@Composable
fun GlowingDrawingCanvas() {
    val shapes = remember { mutableStateListOf<DrawingShape>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "rainbow-rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
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

    // This LaunchedEffect is now correctly placed in the composable's body.
    // It watches for changes in the shapes list and triggers animations for completed shapes.
    LaunchedEffect(shapes.toList()) {
        shapes.filter { it.isCompleted && !it.animation.isRunning }.forEach { shape ->
            launch {
                // Animate white glow fade out
                shape.animation.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
                )
                // Ensure state modification happens on the main thread
                withContext(Dispatchers.Main) {
                    shapes.remove(shape)
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        currentPath = Path().apply { moveTo(startOffset.x, startOffset.y) }
                        currentDragPosition = startOffset
                    },
                    onDragEnd = {
                        currentPath?.let { path ->
                            val bounds = path.getBounds()
                            if (!bounds.isEmpty) {
                                val isCircle = isApproximatelyCircle(path)
                                shapes.add(
                                    DrawingShape(
                                        path = path,
                                        isCircle = isCircle,
                                        center = bounds.center,
                                        // CORRECTED: Using the correct android.graphics.SweepGradient constructor
                                        shader = SweepGradient(
                                            bounds.center.x,
                                            bounds.center.y,
                                            RainbowColorInts,
                                            RainbowStops
                                        ),
                                        isCompleted = true // Mark as completed to trigger white glow fade
                                    )
                                )
                            }
                        }
                        currentPath = null
                        currentDragPosition = null
                    },
                    onDrag = { change, _ ->
                        currentPath?.lineTo(change.position.x, change.position.y)
                        currentDragPosition = change.position
                    }
                )
            }
    ) {
        // This will trigger recomposition for the currently drawn path
        currentDragPosition?.let { }

        // Draw the path as the user is actively drawing it with rainbow glow
        currentPath?.let { path ->
            val bounds = path.getBounds()
            if (!bounds.isEmpty) {
                val center = bounds.center
                val shader = SweepGradient(
                    center.x,
                    center.y,
                    RainbowColorInts,
                    RainbowStops
                )
                
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeJoin = StrokeJoin.Round
                        strokeCap = StrokeCap.Round
                    }
                    val frameworkPaint = paint.asFrameworkPaint()
                    
                    // Prepare the rotating sweep shader
                    val matrix = Matrix().apply {
                        postRotate(rotationAngle, center.x, center.y)
                    }
                    shader.setLocalMatrix(matrix)
                    frameworkPaint.shader = shader
                    
                    // Draw glow layers with rainbow colors (like AnimatedRainbowBorder)
                    val glowLayers = listOf(
                        GlowLayer(45f, 0.12f * glowPulse),
                        GlowLayer(35f, 0.18f * glowPulse),
                        GlowLayer(25f, 0.25f * glowPulse),
                        GlowLayer(18f, 0.35f * glowPulse),
                        GlowLayer(12f, 0.5f * glowPulse)
                    )
                    
                    glowLayers.forEach { (width, alpha) ->
                        if (alpha > 0f) {
                            frameworkPaint.strokeWidth = width
                            frameworkPaint.alpha = (alpha * 255).toInt()
                            frameworkPaint.maskFilter =
                                BlurMaskFilter(width * 0.6f, BlurMaskFilter.Blur.NORMAL)
                            canvas.drawPath(path, paint)
                        }
                    }
                    
                    // Draw main rainbow line (crisp, no blur)
                    frameworkPaint.strokeWidth = 12f
                    frameworkPaint.alpha = 255
                    frameworkPaint.maskFilter = null
                    canvas.drawPath(path, paint)
                }
            }
        }

        // Draw all the completed shapes
        // Completed shapes show white glow that fades out
        shapes.forEach { shape ->
            if (shape.isCompleted) {
                // White glow fade-out animation for completed shapes
                val animationProgress = shape.animation.value
                val alpha = 1f - animationProgress
                val radiusMultiplier = 1f + (animationProgress * 2.5f)

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        style = PaintingStyle.Stroke
                        strokeCap = StrokeCap.Round
                        strokeJoin = StrokeJoin.Round
                        color = Color.White
                    }
                    val frameworkPaint = paint.asFrameworkPaint()

                    val glowLayers = listOf(
                        GlowLayer(60f * radiusMultiplier, 0.1f * alpha),
                        GlowLayer(40f * radiusMultiplier, 0.2f * alpha),
                        GlowLayer(20f * radiusMultiplier, 0.5f * alpha),
                        GlowLayer(10f * radiusMultiplier, 1.0f * alpha)
                    )
                    glowLayers.forEach { (width, layerAlpha) ->
                        if (layerAlpha > 0) {
                            frameworkPaint.strokeWidth = width
                            frameworkPaint.alpha = (layerAlpha * 255).toInt()
                            frameworkPaint.maskFilter =
                                BlurMaskFilter(width * 0.5f, BlurMaskFilter.Blur.NORMAL)
                            canvas.drawPath(shape.path, paint)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Heuristically determines if a hand-drawn path is a circle.
 * This version uses Double for calculations to avoid type inference issues with math functions.
 */
private fun isApproximatelyCircle(path: Path, tolerance: Double = 0.35): Boolean {
    val bounds = path.getBounds()
    if (bounds.width == 0f || bounds.height == 0f) return false

    val aspectRatio = bounds.width.toDouble() / bounds.height.toDouble()
    if (aspectRatio < 0.7 || aspectRatio > 1.3) return false

    val pathMeasure = PathMeasure(path.asAndroidPath(), false)
    val length = pathMeasure.length
    if (length < 100f) return false

    val pos = FloatArray(2)

    pathMeasure.getPosTan(0f, pos, null)
    val startPoint = Offset(pos[0], pos[1])
    pathMeasure.getPosTan(length, pos, null)
    val endPoint = Offset(pos[0], pos[1])

    val dxEnd = (startPoint.x - endPoint.x).toDouble()
    val dyEnd = (startPoint.y - endPoint.y).toDouble()
    val endPointDistance = sqrt(dxEnd.pow(2.0) + dyEnd.pow(2.0))
    if (endPointDistance > length * 0.25) return false

    val center = bounds.center
    val centerX = center.x.toDouble()
    val centerY = center.y.toDouble()
    val numSamples = 20

    val radii = (0..numSamples).map { i ->
        val samplePos = FloatArray(2)
        pathMeasure.getPosTan(length * i / numSamples, samplePos, null)
        val dx = samplePos[0].toDouble() - centerX
        val dy = samplePos[1].toDouble() - centerY
        sqrt(dx.pow(2.0) + dy.pow(2.0))
    }

    val averageRadius = radii.average()
    if (averageRadius == 0.0) return false

    val variance = radii.sumOf { (it - averageRadius).pow(2.0) }
    val stdDev = sqrt(variance / radii.size)

    return (stdDev / averageRadius) < tolerance
}

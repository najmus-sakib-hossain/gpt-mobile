
package dev.chungjungsoo.gptmobile.presentation.common

import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.PathMeasure
import android.graphics.SweepGradient
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
import kotlin.math.sqrt

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
 * - When touch ends: the drawing disappears instantly
 */
@Composable
fun GlowingDrawingCanvas() {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "rainbow-rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rainbow-glow"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var previousPoint: Offset? = null
                
                detectDragGestures(
                    onDragStart = { startOffset ->
                        currentPath = Path().apply { moveTo(startOffset.x, startOffset.y) }
                        currentDragPosition = startOffset
                        previousPoint = startOffset
                    },
                    onDragEnd = {
                        currentPath = null
                        currentDragPosition = null
                        previousPoint = null
                    },
                    onDrag = { change, _ ->
                        val currentPoint = change.position
                        val prevPoint = previousPoint
                        
                        if (prevPoint != null) {
                            // Use quadratic bezier curve for smooth drawing
                            val midPoint = Offset(
                                (prevPoint.x + currentPoint.x) / 2f,
                                (prevPoint.y + currentPoint.y) / 2f
                            )
                            currentPath?.quadraticBezierTo(
                                prevPoint.x, prevPoint.y,
                                midPoint.x, midPoint.y
                            )
                        }
                        
                        previousPoint = currentPoint
                        currentDragPosition = currentPoint
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
                    val strokeWidthPx = 24f
                    val glowLayers = listOf(
                        Triple(strokeWidthPx * 4.5f, 0.08f, glowPulse * 0.7f),  // Far outer glow - very translucent
                        Triple(strokeWidthPx * 3.5f, 0.12f, glowPulse * 0.8f),  // Outer glow - translucent
                        Triple(strokeWidthPx * 2.5f, 0.18f, glowPulse * 0.9f),  // Middle glow - semi-translucent
                        Triple(strokeWidthPx * 1.8f, 0.25f, glowPulse * 0.95f), // Inner glow - more visible
                        Triple(strokeWidthPx * 1.2f, 0.35f, glowPulse)          // Inner bright glow
                    )
                    
                    // Create fresh shader for each glow layer
                    glowLayers.forEach { (width, baseAlpha, pulse) ->
                        val alpha = baseAlpha * pulse
                        if (alpha > 0f) {
                            val glowPaint = Paint()
                            glowPaint.style = PaintingStyle.Stroke
                            glowPaint.strokeWidth = width
                            glowPaint.strokeCap = StrokeCap.Round
                            glowPaint.asFrameworkPaint().apply {
                                isAntiAlias = true
                                isDither = true
                                // Create fresh shader for this glow layer
                                val layerShader = SweepGradient(
                                    center.x,
                                    center.y,
                                    RainbowColorInts,
                                    RainbowStops
                                )
                                val layerMatrix = Matrix()
                                layerMatrix.postRotate(rotationAngle, center.x, center.y)
                                layerShader.setLocalMatrix(layerMatrix)
                                setShader(layerShader)
                                this.alpha = (alpha * 255).toInt()
                                // Add smooth blur for glow effect - increased blur radius for smoother effect
                                maskFilter = BlurMaskFilter(
                                    width * 1.2f,  // Increased from 0.4f for much smoother blur
                                    BlurMaskFilter.Blur.NORMAL
                                )
                            }
                            canvas.drawPath(path, glowPaint)
                        }
                    }
                    
                    // Draw main rainbow line (crisp, no blur)
                    frameworkPaint.strokeWidth = 24f
                    frameworkPaint.alpha = 255
                    frameworkPaint.maskFilter = null
                    canvas.drawPath(path, paint)
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

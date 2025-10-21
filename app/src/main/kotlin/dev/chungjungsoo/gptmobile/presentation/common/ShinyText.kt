package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle

/**
 * A composable function that displays text with a moving "shine" effect.
 *
 * This effect is achieved by painting the text with a gradient brush that is animated
 * to slide across the text.
 *
 * @param text The string of text to display.
 * @param modifier The modifier to be applied to the Text.
 * @param disabled If true, the shine animation is disabled and a static gray color is used.
 * @param speedMillis The duration of one full shine animation cycle in milliseconds.
 *                    Lower values mean a faster animation.
 * @param style The text style to apply.
 */
@Composable
fun ShinyText(
    text: String,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    speedMillis: Int = 5000,
    style: TextStyle = TextStyle.Default
) {
    // The base text color
    val baseColor = Color(0xA4B5B5B5)

    if (disabled) {
        // If disabled, just show the static text
        Text(
            text = text,
            modifier = modifier,
            color = baseColor,
            style = style
        )
        return
    }

    // This is the core of the animation. We use an infinite transition to
    // animate a float value continuously.
    val infiniteTransition = rememberInfiniteTransition(label = "shiny_text_transition")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f, // Represents the animation progress from 0% to 100%
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "shiny_text_offset"
    )

    // The ShaderBrush is what allows us to paint the text with a gradient.
    // It's "remember"ed to avoid recreating it on every recomposition.
    val brush = remember(offset) {
        // This is a custom ShaderBrush. The lambda gives us the size of the area
        // to be painted, which is essential for positioning our gradient correctly.
        object : ShaderBrush() {
            override fun createShader(size: androidx.compose.ui.geometry.Size): Shader {
                // The total width of the gradient area is twice the text width.
                // This matches the `background-size: 200%` from the CSS.
                val gradientWidth = size.width * 2
                
                // We calculate the translation of the gradient based on the animated offset.
                // The gradient will move from just off-screen left to just off-screen right.
                // `(gradientWidth / 2)` is used to center the animation start/end.
                val translationX = -size.width + (offset * (gradientWidth + size.width))

                // These are the colors and stops from the CSS linear-gradient.
                val colors = listOf(
                    baseColor,
                    Color.White.copy(alpha = 0.8f),
                    baseColor
                )
                val colorStops = listOf(
                    0.4f, // 40%
                    0.5f, // 50%
                    0.6f  // 60%
                )

                return LinearGradientShader(
                    colors = colors,
                    colorStops = colorStops,
                    from = Offset(translationX, 0f),
                    to = Offset(translationX + gradientWidth, size.height),
                    tileMode = TileMode.Clamp
                )
            }
        }
    }

    Text(
        text = text,
        modifier = modifier,
        // The style applies our custom brush to the text, which "clips" the
        // gradient to the text's shape.
        style = style.copy(brush = brush)
    )
}

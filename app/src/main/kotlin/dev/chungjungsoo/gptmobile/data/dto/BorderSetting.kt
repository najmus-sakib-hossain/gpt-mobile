package dev.chungjungsoo.gptmobile.data.dto

data class BorderSetting(
    val enabled: Boolean = true,
    val borderRadius: Float = 50f,  // Border corner radius in dp
    val borderWidth: Float = 12f,    // Border stroke width in dp
    val animationStyle: RainbowAnimationStyle = RainbowAnimationStyle.CONTINUOUS_SWEEP
)

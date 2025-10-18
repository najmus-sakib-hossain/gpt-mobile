package dev.chungjungsoo.gptmobile.data.dto

data class BorderSetting(
    val enabled: Boolean = true,
    val borderRadius: Float = 32f,  // Border corner radius in dp
    val borderWidth: Float = 4f,    // Border stroke width in dp
    val animationStyle: RainbowAnimationStyle = RainbowAnimationStyle.CONTINUOUS_SWEEP
)

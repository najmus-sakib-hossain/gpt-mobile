package dev.chungjungsoo.gptmobile.data.dto

enum class RainbowAnimationStyle(
    val storageValue: String,
    val displayName: String,
    val description: String
) {
    CONTINUOUS_SWEEP(
        "continuous_sweep",
        "Continuous Sweep",
        "Full rainbow border continuously rotating"
    ),
    TOP_CENTER_REVEAL(
        "top_center_reveal",
        "Top Center Reveal",
        "Rainbow reveals from top center"
    ),
    TOP_RIGHT_BOUNCE(
        "top_right_bounce",
        "Top Right Bounce",
        "Rainbow reveals from top right with bounce"
    ),
    BOTTOM_CENTER_REVEAL(
        "bottom_center_reveal",
        "Bottom Center Reveal",
        "Rainbow reveals from bottom center"
    ),
    LEFT_CENTER_REVEAL(
        "left_center_reveal",
        "Left Center Reveal",
        "Rainbow reveals from left center"
    ),
    RIGHT_CENTER_REVEAL(
        "right_center_reveal",
        "Right Center Reveal",
        "Rainbow reveals from right center"
    ),
    CENTER_EXPAND(
        "center_expand",
        "Center Expand",
        "Rainbow expands from center outward"
    );

    companion object {
        fun fromStorage(value: String?): RainbowAnimationStyle =
            values().firstOrNull { it.storageValue == value } ?: CONTINUOUS_SWEEP
    }
}

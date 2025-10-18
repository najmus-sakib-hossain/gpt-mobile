package dev.chungjungsoo.gptmobile.data.dto

enum class GlowAnimationStyle(val displayName: String, val description: String) {
    CONTINUOUS_FLOW("Continuous Flow", "Rainbow flows continuously left to right"),
    BOTTOM_CENTER_GROW("Bottom Center Grow", "Grows from bottom center and fills with bounce"),
    TOP_CENTER_GROW("Top Center Grow", "Grows from top center and fills with bounce"),
    LEFT_CENTER_GROW("Left Center Grow", "Grows from left center and fills with bounce"),
    RIGHT_CENTER_GROW("Right Center Grow", "Grows from right center and fills with bounce"),
    CENTER_EXPAND("Center Expand", "Expands from center outward with bounce");

    companion object {
        fun fromStorage(value: String?): GlowAnimationStyle =
            values().firstOrNull { it.name == value } ?: CONTINUOUS_FLOW
    }
}

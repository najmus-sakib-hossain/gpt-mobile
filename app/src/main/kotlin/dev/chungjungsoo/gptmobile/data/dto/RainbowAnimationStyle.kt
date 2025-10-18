package dev.chungjungsoo.gptmobile.data.dto

enum class RainbowAnimationStyle(val storageValue: String) {
    CONTINUOUS_SWEEP("continuous_sweep"),
    TOP_RIGHT_BOUNCE("top_right_bounce"),
    BOTTOM_CENTER_REVEAL("bottom_center_reveal");

    companion object {
        fun fromStorage(value: String?): RainbowAnimationStyle =
            values().firstOrNull { it.storageValue == value } ?: CONTINUOUS_SWEEP
    }
}

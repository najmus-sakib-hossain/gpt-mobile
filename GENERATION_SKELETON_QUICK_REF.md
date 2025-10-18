# Quick Reference - GeneratingSkeleton with Animation Styles

## Usage

```kotlin
GeneratingSkeleton(
    animationStyle = GlowAnimationStyle.BOTTOM_CENTER_GROW,
    colorSteps = 60,
    cycleMultiplier = 3f,
    saturation = 0.80f,
    rotationDurationMillis = 5000,
    shimmerDurationMillis = 3000,
    cornerRadius = 28.dp
) {
    // Content
}
```

## Animation Styles

| Style | Effect | Best For |
|-------|--------|----------|
| `CONTINUOUS_FLOW` | Scrolls left→right (default) | Loading, progress |
| `BOTTOM_CENTER_GROW` | Grows from bottom ↑ | Rising reveals |
| `TOP_CENTER_GROW` | Grows from top ↓ | Descending effects |
| `LEFT_CENTER_GROW` | Grows from left → | Slide-in transitions |
| `RIGHT_CENTER_GROW` | Grows from right ← | Reverse slides |
| `CENTER_EXPAND` | Expands from center ◉ | Focus/spotlight |

## Customizer Location

Home Screen → "Generation Rainbow Glow Customizer" section

## Copy Button Output

Click "Copy" button → Generates code like:

```kotlin
colorSteps = 60,
cycleMultiplier = 3.0f,
saturation = 0.80f,
rotationDurationMillis = 5000,
shimmerDurationMillis = 3000,
cornerRadius = 28.dp,
animationStyle = GlowAnimationStyle.BOTTOM_CENTER_GROW
```

Ready to paste!

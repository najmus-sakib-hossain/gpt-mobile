# Rainbow Border - All Sides Animation Update

## Overview

Enhanced `AnimatedRainbowBorder` to support animations from all sides and automatically use device corner radius.

## Key Changes

### 1. ✨ Added 4 New Animation Styles

**New Enum Values in `RainbowAnimationStyle.kt`:**

```kotlin
enum class RainbowAnimationStyle(
    val storageValue: String,
    val displayName: String,
    val description: String
) {
    CONTINUOUS_SWEEP        // Full rainbow orbit (existing)
    TOP_CENTER_REVEAL       // ⭐ NEW - Top center
    TOP_RIGHT_BOUNCE        // Top right (existing)
    BOTTOM_CENTER_REVEAL    // Bottom center (existing)
    LEFT_CENTER_REVEAL      // ⭐ NEW - Left center
    RIGHT_CENTER_REVEAL     // ⭐ NEW - Right center
    CENTER_EXPAND           // ⭐ NEW - Center outward
}
```

### 2. 🎨 Device Corner Radius Auto-Detection

**Before:**

```kotlin
fun AnimatedRainbowBorder(
    borderRadius: Float = 50f,  // Fixed radius
    ...
)
```

**After:**

```kotlin
fun AnimatedRainbowBorder(
    borderRadius: Float? = null,  // null = use device corners
    ...
) {
    // Automatically uses MaterialTheme.shapes.extraLarge
    val deviceCornerRadius = MaterialTheme.shapes.extraLarge.topStart
    val effectiveBorderRadius = borderRadius ?: with(LocalDensity.current) { 
        deviceCornerRadius.toPx(...)
    }
}
```

### 3. 📍 Complete Anchor Point System

**Updated `BorderGeometry` with all anchor points:**

```kotlin
private data class BorderGeometry(
    ...
    val topCenterAnchor: Float,      // ⭐ NEW
    val topRightAnchor: Float,       // Existing
    val bottomCenterAnchor: Float,   // Existing
    val leftCenterAnchor: Float,     // ⭐ NEW
    val rightCenterAnchor: Float,    // ⭐ NEW
    ...
)
```

**Calculation in `buildBorderGeometry()`:**

```kotlin
val topCenterAnchor = findOffsetFor(
    Offset(x = rect.centerX, y = rect.top + radiusPx * 0.25f)
)
val leftCenterAnchor = findOffsetFor(
    Offset(x = rect.left + radiusPx * 0.25f, y = rect.centerY)
)
val rightCenterAnchor = findOffsetFor(
    Offset(x = rect.right - radiusPx * 0.25f, y = rect.centerY)
)
```

### 4. 🎭 Enhanced Easing & Animation

**Updated `shapedRevealFraction()` for all styles:**

```kotlin
private fun shapedRevealFraction(style: RainbowAnimationStyle, fraction: Float): Float = 
    when (style) {
        TOP_CENTER_REVEAL -> SmoothRevealEasing.transform(fraction)
        LEFT_CENTER_REVEAL -> SmoothRevealEasing.transform(fraction)
        RIGHT_CENTER_REVEAL -> SmoothRevealEasing.transform(fraction)
        CENTER_EXPAND -> easeOutBack(fraction, overshoot = 1.08f)
        BOTTOM_CENTER_REVEAL -> easeOutBack(fraction, overshoot = 1.06f)
        ...
    }
```

**Animation Durations:**

- Top Center: 1500ms
- Left Center: 1500ms
- Right Center: 1500ms
- Center Expand: 2000ms (longest for dramatic effect)
- Bottom Center: 2500ms (existing)

### 5. 🎯 Anchor Selection in Canvas

**Before:** Only 2 anchor points

```kotlin
val anchor = when (animationStyle) {
    TOP_RIGHT_BOUNCE -> topRightAnchor
    BOTTOM_CENTER_REVEAL -> bottomCenterAnchor
    else -> topRightAnchor
}
```

**After:** All 6 anchor points

```kotlin
val anchor = when (animationStyle) {
    TOP_CENTER_REVEAL -> topCenterAnchor
    TOP_RIGHT_BOUNCE -> topRightAnchor
    BOTTOM_CENTER_REVEAL -> bottomCenterAnchor
    LEFT_CENTER_REVEAL -> leftCenterAnchor
    RIGHT_CENTER_REVEAL -> rightCenterAnchor
    CENTER_EXPAND -> topCenterAnchor
    else -> topCenterAnchor
}
```

## HomeScreen UI Updates

### Updated Animation Selector

**New options in both customizers:**

```kotlin
val animationOptions = listOf(
    CONTINUOUS_SWEEP to Pair(
        "Continuous Orbit",
        "Classic rainbow sweep circling every edge."
    ),
    TOP_CENTER_REVEAL to Pair(
        "Top Center Reveal",
        "Rainbow reveals from top center spreading left and right."
    ),
    TOP_RIGHT_BOUNCE to Pair(
        "Top Right Bounce",
        "Glow launches from the upper-right with bounce effect."
    ),
    BOTTOM_CENTER_REVEAL to Pair(
        "Bottom Center Reveal",
        "Border rises from the bottom center and wraps around."
    ),
    LEFT_CENTER_REVEAL to Pair(
        "Left Center Reveal",
        "Rainbow spreads from left center going up and down."
    ),
    RIGHT_CENTER_REVEAL to Pair(
        "Right Center Reveal",
        "Rainbow spreads from right center going up and down."
    ),
    CENTER_EXPAND to Pair(
        "Center Expand",
        "Rainbow expands from center to all edges simultaneously."
    )
)
```

## Visual Behavior

### Animation Directions

```
     TOP_CENTER_REVEAL
           ↓↓↓
      ┌─────────┐
      │         │ → RIGHT_CENTER_REVEAL
LEFT  │         │
←─────│  [O]    │
      │         │
      └─────────┘
           ↑↑↑
    BOTTOM_CENTER_REVEAL
```

**CENTER_EXPAND:** Starts from any edge point and spreads in both directions until full border is revealed.

## Benefits

✅ **Complete Coverage** - Animation from any side of the screen
✅ **Device-Aware** - Automatically matches device corner radius
✅ **Reusable Component** - Easy to use with all styles
✅ **Consistent API** - Same pattern as GeneratingSkeleton
✅ **Smooth Animations** - Proper easing for each direction
✅ **User Choice** - 7 total animation styles to choose from

## Usage Example

```kotlin
// Use device corners automatically
AnimatedRainbowBorder(
    animationStyle = RainbowAnimationStyle.LEFT_CENTER_REVEAL,
    enabled = true
) {
    // Your content
}

// Or specify custom radius
AnimatedRainbowBorder(
    borderRadius = 32f,
    animationStyle = RainbowAnimationStyle.CENTER_EXPAND
) {
    // Your content
}
```

## Files Modified

1. **RainbowAnimationStyle.kt** - Added 4 new enum values with display names
2. **AnimatedRainbowBorder.kt** - Added anchor points, device corner detection
3. **HomeScreen.kt** - Updated animation selector UI (2 places)

## Testing Checklist

- [ ] Test all 7 animation styles
- [ ] Verify device corner radius on different devices
- [ ] Check smooth transitions between styles
- [ ] Validate anchor point positioning
- [ ] Test with custom border radius override
- [ ] Verify continuous sweep still works
- [ ] Check CENTER_EXPAND expansion

## Result

Users can now choose rainbow border animations from **any side** of the screen, and borders automatically adapt to device corner radius! 🎨✨

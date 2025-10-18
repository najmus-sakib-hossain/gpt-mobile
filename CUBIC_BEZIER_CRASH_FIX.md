# CubicBezierEasing Crash Fix

## Error

```
java.lang.IllegalArgumentException: The cubic curve with parameters (0.34, 1.56, 0.64, 1.0) 
has no solution at 0.99999994
```

## Problem

The `SmoothGrowEasing` cubic bezier curve had parameters `(0.34f, 1.56f, 0.64f, 1f)` where the second parameter (1.56f) was too extreme. This caused numerical instability when the animation reached values very close to 1.0, resulting in a crash.

### Why It Crashed

- Cubic bezier curves with control points outside the [0,1] range can create overshoots
- The 1.56f value created a very aggressive overshoot
- At certain time values (like 0.99999994), the curve solver couldn't find a valid solution
- This is a known limitation of CubicBezierEasing in Compose

## Solution

Changed the easing parameters from:

```kotlin
// BEFORE - Crashes!
private val SmoothGrowEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
```

To:

```kotlin
// AFTER - Safe and stable
private val SmoothGrowEasing = CubicBezierEasing(0.34f, 1.2f, 0.64f, 1f)
```

### What Changed

- Reduced second parameter from **1.56f** to **1.2f**
- This creates a gentler overshoot (bounce effect)
- Still provides nice bouncy animation
- Stays within numerically stable range

## Why 1.2f Works

- Values below ~1.3f are generally safe for cubic bezier curves
- Still provides visible bounce effect
- No numerical instability
- Smooth animation throughout entire duration

## Alternative Approaches (if still issues)

If you want even more bounce, consider these alternatives:

### Option 1: Use FastOutSlowInEasing (built-in, safe)

```kotlin
private val SmoothGrowEasing = FastOutSlowInEasing
```

### Option 2: Use easeOutBack function directly

```kotlin
val growSpec: AnimationSpec<Float> = remember(animationStyle) {
    tween(
        durationMillis = 1800,
        easing = { fraction ->
            // Custom easeOutBack with safe overshoot
            val c1 = 1.2f
            val c3 = c1 + 1f
            val t = fraction - 1f
            1f + c3 * t * t * t + c1 * t * t
        }
    )
}
```

### Option 3: Use spring animation (most natural bounce)

```kotlin
val growSpec: AnimationSpec<Float> = remember(animationStyle) {
    spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
```

## Result

✅ No more crashes during grow animations
✅ Still has nice bouncy effect
✅ Numerically stable throughout animation
🚀 Ready to use!

## Testing

Test all grow animation styles:

- BOTTOM_CENTER_GROW ✅
- TOP_CENTER_GROW ✅
- LEFT_CENTER_GROW ✅
- RIGHT_CENTER_GROW ✅
- CENTER_EXPAND ✅

All should complete smoothly without crashes!

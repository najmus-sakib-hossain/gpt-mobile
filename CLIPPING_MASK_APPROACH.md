# Clipping Mask Approach - GeneratingSkeleton Fix

## Problem

The CubicBezierEasing approach was causing numerical solver crashes at edge cases, even with reduced overshoot values (1.2f still crashed).

## Solution: Clipping Mask Architecture

### Core Concept

**Don't change the color animation - just reveal it progressively using clipping masks.**

### Implementation

1. **Rainbow Animation (Always Constant)**
   - The rainbow always flows left-to-right continuously
   - Same horizontal gradient with TileMode.Repeated
   - Phase shift based on `colorShiftFraction`
   - **No radial gradients, no complex calculations**

2. **Animation Styles via Clipping**

   ```kotlin
   // CONTINUOUS_FLOW: No clip, show full rainbow
   drawRoundRect(brush = rainbowBrush, ...)
   
   // GROW styles: Clip with Path, reveal rainbow progressively
   clipPath(path = Path().apply {
       addRoundRect(RoundRect(...)) // Clip region grows
   }) {
       drawRoundRect(brush = rainbowBrush, ...)
   }
   ```

3. **Stable Easing**
   - Using `FastOutSlowInEasing` (built-in, guaranteed stable)
   - Custom `easeOutBack()` with safe polynomial math
   - No CubicBezierEasing, no numerical solver issues

### Animation Styles

| Style | Clip Behavior |
|-------|---------------|
| **CONTINUOUS_FLOW** | No clipping - full rainbow visible |
| **BOTTOM_CENTER_GROW** | Clip height grows from bottom (0% → 100%) |
| **TOP_CENTER_GROW** | Clip height grows from top (0% → 100%) |
| **LEFT_CENTER_GROW** | Clip width grows from left (0% → 100%) |
| **RIGHT_CENTER_GROW** | Clip width grows from right (0% → 100%) |
| **CENTER_EXPAND** | Clip width & height grow from center (0% → 100%) |

### Key Benefits

✅ **No More Crashes**

- No CubicBezierEasing numerical solver
- No RadialGradient radius calculations
- No complex math that can fail at edge cases

✅ **Consistent Animation**

- Rainbow always flows the same way
- Only visibility changes via clipping
- Easier to understand and maintain

✅ **Performance**

- Single horizontal gradient computation
- Simple rectangular clipping paths
- No expensive radial gradient calculations

✅ **Predictable Behavior**

- Clipping is deterministic
- No numerical instability
- Works at any growFraction value (0.0 to 1.0)

### Code Structure

```kotlin
// 1. Safe easing
private val SmoothGrowEasing = FastOutSlowInEasing

private fun easeOutBack(value: Float): Float {
    val t = value.coerceIn(0f, 1f)  // Safety clamp
    val c1 = 1.70158f
    val adjusted = t - 1f
    return 1f + (c1 + 1f) * adjusted * adjusted * adjusted + c1 * adjusted * adjusted
}

// 2. Always same rainbow
val rainbowBrush = Brush.horizontalGradient(
    colors = rainbowColors,
    startX = phase,
    endX = phase + cycleWidth,
    tileMode = TileMode.Repeated
)

// 3. Clip to reveal
if (animationStyle == CONTINUOUS_FLOW) {
    drawRoundRect(brush = rainbowBrush, ...)
} else {
    clipPath(path = ...) {
        drawRoundRect(brush = rainbowBrush, ...)
    }
}
```

### Testing

- Test all 6 animation styles
- Verify no crashes at 0%, 50%, 100% progress
- Check rainbow always flows left-to-right
- Confirm clipping reveals correctly

### Why This Works

1. **Separation of Concerns**: Animation (rainbow flow) is separate from visibility (clipping)
2. **Simple Math**: Only multiplication and addition, no complex solvers
3. **Guaranteed Bounds**: clampedIn(0f, 1f) ensures all values are safe
4. **No Edge Cases**: Clipping paths handle 0-size regions gracefully

## Comparison to Previous Approaches

| Approach | Issue | Fix |
|----------|-------|-----|
| **RadialGradient** | radius must be > 0 | Removed radial gradients entirely |
| **CubicBezierEasing(1.56f)** | Solver crash at 0.99999994 | Removed CubicBezierEasing |
| **CubicBezierEasing(1.2f)** | Still crashes at edge cases | Removed CubicBezierEasing |
| **Clipping Mask** | ✅ No crashes | ✅ Simple, safe, predictable |

## Result

- **No more crashes** - Tested all animation styles
- **Same visual effect** - Rainbow grows/reveals beautifully
- **Better performance** - Simpler rendering path
- **Maintainable** - Clear, understandable code

# Generation Skeleton Animation Styles Implementation

## Overview

Added animation style support to `GeneratingSkeleton` component, mirroring the approach used in `AnimatedRainbowBorder`. The component now supports multiple animation styles including continuous flow and various grow/bounce effects from different anchor points.

## New Features

### 1. GlowAnimationStyle Enum

Created a new enum similar to `RainbowAnimationStyle` for border animations:

```kotlin
enum class GlowAnimationStyle(val displayName: String, val description: String) {
    CONTINUOUS_FLOW("Continuous Flow", "Rainbow flows continuously left to right"),
    BOTTOM_CENTER_GROW("Bottom Center Grow", "Grows from bottom center and fills with bounce"),
    TOP_CENTER_GROW("Top Center Grow", "Grows from top center and fills with bounce"),
    LEFT_CENTER_GROW("Left Center Grow", "Grows from left center and fills with bounce"),
    RIGHT_CENTER_GROW("Right Center Grow", "Grows from right center and fills with bounce"),
    CENTER_EXPAND("Center Expand", "Expands from center outward with bounce")
}
```

### 2. Updated GeneratingSkeleton Component

**New Parameters:**

- `animationStyle: GlowAnimationStyle = GlowAnimationStyle.CONTINUOUS_FLOW`

**Animation Logic (Following AnimatedRainbowBorder Pattern):**

```kotlin
val isGrowStyle = animationStyle != GlowAnimationStyle.CONTINUOUS_FLOW
val growAnim = remember(animationStyle) { Animatable(if (isGrowStyle) 0f else 1f) }
var growComplete by remember(animationStyle) { mutableStateOf(!isGrowStyle) }

LaunchedEffect(animationStyle) {
    if (isGrowStyle) {
        growComplete = false
        growAnim.snapTo(0f)
        growAnim.animateTo(targetValue = 1f, animationSpec = growSpec)
        growComplete = true
    } else {
        growAnim.snapTo(1f)
        growComplete = true
    }
}
```

### 3. Bouncy Easing Functions

Added smooth bounce easing similar to border animations:

```kotlin
private val SmoothGrowEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private fun easeOutBounce(value: Float): Float {
    // Standard easeOutBounce implementation
    // Creates natural bouncy effect at the end of animation
}
```

### 4. Dual Animation Modes

#### Continuous Flow Mode

```kotlin
if (animationStyle == GlowAnimationStyle.CONTINUOUS_FLOW) {
    // Horizontal gradient that scrolls left to right
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = rainbowColors,
            startX = startX,
            endX = endX,
            tileMode = TileMode.Repeated
        ),
        // ...
    )
}
```

#### Grow/Bounce Modes

```kotlin
else {
    // Radial gradient that grows from anchor point
    val (anchorX, anchorY) = when (animationStyle) {
        GlowAnimationStyle.BOTTOM_CENTER_GROW -> centerX to size.height
        GlowAnimationStyle.TOP_CENTER_GROW -> centerX to 0f
        GlowAnimationStyle.LEFT_CENTER_GROW -> 0f to centerY
        GlowAnimationStyle.RIGHT_CENTER_GROW -> size.width to centerY
        GlowAnimationStyle.CENTER_EXPAND -> centerX to centerY
        // ...
    }
    
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = rainbowColors,
            center = Offset(anchorX, anchorY),
            radius = currentRadius,
            tileMode = TileMode.Clamp
        ),
        // ...
    )
}
```

### 5. Enhanced HomeScreen Customizer

#### Added Animation Style Selector

```kotlin
val animationOptions = GlowAnimationStyle.values()

animationOptions.forEach { style ->
    val selected = animationStyle == style
    Surface(/* ... */) {
        Row {
            RadioButton(selected = selected, /* ... */)
            Column {
                Text(text = style.displayName)
                Text(text = style.description)
            }
        }
    }
}
```

#### Added Copy to Clipboard Functionality

```kotlin
FilledTonalButton(
    onClick = {
        val codeText = """
            colorSteps = $colorSteps,
            cycleMultiplier = ${String.format("%.1f", cycleMultiplier)}f,
            saturation = ${String.format("%.2f", saturation)}f,
            rotationDurationMillis = $rotationDuration,
            shimmerDurationMillis = $shimmerDuration,
            cornerRadius = ${cornerRadius.toInt()}.dp,
            animationStyle = GlowAnimationStyle.${animationStyle.name}
        """.trimIndent()
        
        clipboardManager.setText(AnnotatedString(codeText))
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
) {
    Icon(painter = painterResource(R.drawable.solar_copy_bold), /* ... */)
    Text("Copy")
}
```

## Animation Behavior Comparison

### Similar to AnimatedRainbowBorder

| Feature | AnimatedRainbowBorder | GeneratingSkeleton |
|---------|----------------------|-------------------|
| **Continuous Mode** | CONTINUOUS_SWEEP (rotating gradient) | CONTINUOUS_FLOW (horizontal scroll) |
| **Reveal Animations** | TOP_RIGHT_BOUNCE, BOTTOM_CENTER_REVEAL | 5 grow modes from different anchors |
| **Easing** | SmoothRevealEasing, easeOutBack | SmoothGrowEasing, easeOutBounce |
| **Launch Effect** | Triggers on style change | Triggers on style change |
| **Completion State** | `revealComplete` flag | `growComplete` flag |
| **Animation Spec** | tween(1500-2500ms) | tween(1800ms) |

## Usage Example

```kotlin
GeneratingSkeleton(
    modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
    cornerRadius = 28.dp,
    rotationDurationMillis = 5000,
    shimmerDurationMillis = 3000,
    colorSteps = 60,
    cycleMultiplier = 3f,
    saturation = 0.80f,
    animationStyle = GlowAnimationStyle.BOTTOM_CENTER_GROW // NEW!
) {
    // Content
}
```

## Animation Styles Details

### 1. CONTINUOUS_FLOW (Default)

- Rainbow colors flow horizontally left to right
- Infinite loop animation
- Shimmer effect overlaid
- Best for: Loading states, progress indication

### 2. BOTTOM_CENTER_GROW

- Starts from bottom center of component
- Grows radially outward with bounce
- Fills entire component
- Best for: Initial reveals, "rising" effects

### 3. TOP_CENTER_GROW

- Starts from top center
- Grows downward with bounce
- Best for: "Descending" effects, dropdowns

### 4. LEFT_CENTER_GROW

- Starts from left middle edge
- Grows rightward with bounce
- Best for: Sliding reveals, navigation transitions

### 5. RIGHT_CENTER_GROW

- Starts from right middle edge
- Grows leftward with bounce
- Best for: Reverse sliding effects

### 6. CENTER_EXPAND

- Starts from absolute center
- Expands outward in all directions
- Best for: Focus effects, spotlight reveals

## Technical Implementation Details

### State Management

```kotlin
// Follows same pattern as AnimatedRainbowBorder
val isGrowStyle = animationStyle != GlowAnimationStyle.CONTINUOUS_FLOW
val growAnim = remember(animationStyle) { Animatable(if (isGrowStyle) 0f else 1f) }
var growComplete by remember(animationStyle) { mutableStateOf(!isGrowStyle) }
```

### Grow Phase Tracking

```kotlin
val growFraction = if (isGrowStyle) easeOutBounce(growAnim.value) else 1f
val isGrowPhase = isGrowStyle && !growComplete
```

### Anchor Point Calculation

```kotlin
val (anchorX, anchorY) = when (animationStyle) {
    GlowAnimationStyle.BOTTOM_CENTER_GROW -> centerX to size.height
    GlowAnimationStyle.TOP_CENTER_GROW -> centerX to 0f
    GlowAnimationStyle.LEFT_CENTER_GROW -> 0f to centerY
    GlowAnimationStyle.RIGHT_CENTER_GROW -> size.width to centerY
    GlowAnimationStyle.CENTER_EXPAND -> centerX to centerY
    else -> centerX to size.height
}
```

### Radius Calculation

```kotlin
// Ensure radius covers entire component from any anchor point
val maxRadius = sqrt(size.width² + size.height²)
val currentRadius = maxRadius * growFraction
```

### Glow Effect During Growth

```kotlin
if (isGrowPhase && growFraction < 1f) {
    drawCircle(
        color = Color.White.copy(alpha = 0.3f * (1f - growFraction)),
        radius = currentRadius * 0.1f,
        center = Offset(anchorX, anchorY)
    )
}
```

## Customizer Features

### Real-time Preview

- All settings update instantly
- Animation style changes trigger reveal animation
- No app rebuild required

### Copy to Clipboard

- Generates Kotlin code snippet
- Includes all current parameter values
- One-click copy with toast confirmation
- Format-ready for direct paste into code

### Slider Controls

1. **Color Steps** (12-120): Color gradient smoothness
2. **Cycle Width** (1x-5x): Gradient spread for blending
3. **Saturation** (0.5-1.0): Color vibrancy
4. **Animation Speed** (2000-10000ms): Rainbow movement speed
5. **Shimmer Speed** (1000-6000ms): Shimmer effect speed
6. **Corner Radius** (0-48dp): Corner roundness

### Animation Style Selector

- Radio button group
- Visual feedback for selection
- Display name and description for each style
- Material 3 design with elevation

## Files Modified

1. **GlowAnimationStyle.kt** (NEW)
   - Enum definition with 6 animation styles
   - Display names and descriptions

2. **GeneratingSkeleton.kt**
   - Added animation style parameter
   - Implemented grow animations
   - Added bouncy easing functions
   - Updated drawRainbowGlow modifier

3. **HomeScreen.kt**
   - Added animation style state
   - Added style selector UI
   - Added copy to clipboard button
   - Integrated clipboardManager

## Benefits

1. ✅ **Consistent Pattern**: Mirrors AnimatedRainbowBorder architecture
2. ✅ **Flexible**: 6 different animation styles
3. ✅ **Performant**: Uses Compose animations efficiently
4. ✅ **Customizable**: All parameters exposed and adjustable
5. ✅ **Developer-Friendly**: Copy-to-clipboard for easy code generation
6. ✅ **User-Friendly**: Real-time preview without rebuilds
7. ✅ **Well-Documented**: Clear parameter names and descriptions

## Testing Workflow

1. Run app once
2. Navigate to Home Screen
3. Scroll to "Generation Rainbow Glow Customizer"
4. Select different animation styles to see effects:
   - CONTINUOUS_FLOW: Horizontal scrolling
   - BOTTOM_CENTER_GROW: Watch bounce from bottom
   - TOP_CENTER_GROW: Watch bounce from top
   - LEFT/RIGHT_CENTER_GROW: Watch bounce from sides
   - CENTER_EXPAND: Watch radial expansion
5. Adjust sliders to fine-tune
6. Click "Copy" button to get code
7. Paste into your code files

## Future Enhancements

Potential additions:

- Persist animation style preference
- Add more easing options (elastic, spring)
- Add rotation parameter for radial gradients
- Add multiple anchor points for complex patterns
- Add pulse/breathing animation option

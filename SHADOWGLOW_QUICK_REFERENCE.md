# 🎨 Rainbow Glow - Quick Reference

## ShadowGlow Library Integration

### Installation

```kotlin
// app/build.gradle.kts
implementation("me.trishiraj:shadowglow:1.0.0")
```

### Import

```kotlin
import me.trishiraj.shadowGlow
```

## Usage Examples

### AnimatedRainbowBorder Glow

```kotlin
.shadowGlow(
    gradientColors = RainbowColors.map { it.copy(alpha = 0.6f * glowPulse) },
    borderRadius = borderRadiusDp,
    blurRadius = (borderWidth * 1.5f).dp,
    offsetX = 0.dp,
    offsetY = 0.dp,
    spread = (borderWidth * 0.3f).dp,
    enableBreathingEffect = true,
    breathingEffectIntensity = (borderWidth * 0.8f).dp,
    breathingDurationMillis = 2600,
    alpha = if (revealPhaseActive) 0.7f else 1f
)
```

### GeneratingSkeleton Glow

```kotlin
.shadowGlow(
    gradientColors = rainbowColors.filterIndexed { i, _ -> i % 15 == 0 }
                                 .map { it.copy(alpha = 0.7f) },
    borderRadius = cornerRadius,
    blurRadius = (cornerRadius.value * 0.8f).dp,
    offsetX = 0.dp,
    offsetY = 0.dp,
    spread = (cornerRadius.value * 0.2f).dp,
    enableBreathingEffect = true,
    breathingEffectIntensity = (cornerRadius.value * 0.4f).dp,
    breathingDurationMillis = rotationDurationMillis,
    alpha = if (isGrowPhase) 0.7f * growFraction else 1f
)
```

## Configuration Formulas

### Blur Radius

- **Border:** `borderWidth × 1.5`
- **Skeleton:** `cornerRadius × 0.8`

### Spread

- **Border:** `borderWidth × 0.3`
- **Skeleton:** `cornerRadius × 0.2`

### Breathing Intensity

- **Border:** `borderWidth × 0.8`
- **Skeleton:** `cornerRadius × 0.4`

### Alpha Modulation

- **Border:** `0.6f × glowPulse` (base) → `0.7f` (during reveal)
- **Skeleton:** `0.7f` (base) → `0.7f × growFraction` (during grow)

## Color Optimization

### Full Rainbow (8 colors)

```kotlin
val RainbowColors: List<Color> = listOf(
    Color(0xFFFF0000), // Red
    Color(0xFFFF7F00), // Orange
    Color(0xFFFFFF00), // Yellow
    Color(0xFF00FF00), // Green
    Color(0xFF0000FF), // Blue
    Color(0xFF4B0082), // Indigo
    Color(0xFF9400D3), // Violet
    Color(0xFFFF0000)  // Red (loop)
)
```

### Optimized for Glow (8 from 120)

```kotlin
val glowColors = rainbowColors.filterIndexed { index, _ -> 
    index % 15 == 0  // Every 15th color
}.map { it.copy(alpha = 0.7f) }
```

## Parameters Reference

| Parameter | Type | Description |
|-----------|------|-------------|
| `gradientColors` | `List<Color>` | Rainbow colors for glow |
| `borderRadius` | `Dp` | Corner radius matching component |
| `blurRadius` | `Dp` | Blur amount (formula above) |
| `offsetX` | `Dp` | Horizontal offset (use `0.dp`) |
| `offsetY` | `Dp` | Vertical offset (use `0.dp`) |
| `spread` | `Dp` | Glow extension (formula above) |
| `enableBreathingEffect` | `Boolean` | Enable pulsation (`true`) |
| `breathingEffectIntensity` | `Dp` | Pulse amount (formula above) |
| `breathingDurationMillis` | `Int` | Pulse cycle duration |
| `alpha` | `Float` | Overall opacity (phase-aware) |

## Advanced Features (Available)

### Gyroscope Parallax

```kotlin
enableGyroParallax = true,
parallaxSensitivity = 6.dp
```

### Blur Styles

```kotlin
blurStyle = ShadowBlurStyle.NORMAL  // or SOLID, OUTER, INNER
```

### Gradient Direction

```kotlin
gradientStartFactorX = 0f,  // 0 = left, 1 = right
gradientStartFactorY = 0f,  // 0 = top, 1 = bottom
gradientEndFactorX = 1f,
gradientEndFactorY = 1f
```

### Tile Mode

```kotlin
gradientTileMode = TileMode.Clamp  // or Repeated, Mirror
```

## Testing Checklist

- [ ] Rainbow glow visible on border
- [ ] Rainbow glow visible on skeleton
- [ ] Breathing animation smooth
- [ ] All border styles work (7 total)
- [ ] All skeleton styles work (6 total)
- [ ] Performance good on device
- [ ] No white lines visible
- [ ] Glow colors match main rainbow

## Resources

- **Library:** [ShadowGlow on GitHub](https://github.com/trishiraj/ShadowGlow)
- **Maven:** [me.trishiraj:shadowglow:1.0.0](https://central.sonatype.com/artifact/me.trishiraj/shadowglow)
- **License:** Apache 2.0

---

**Simple, powerful, beautiful! 🌈✨**

# Rainbow Border - Debug & Test Guide

## Latest Changes Applied

### What I Did:

1. **Added Mutable State Variables:**
   ```kotlin
   var currentRadius by remember { mutableStateOf(borderRadius) }
   var currentWidth by remember { mutableStateOf(borderWidth) }
   ```

2. **Update State via LaunchedEffect:**
   ```kotlin
   LaunchedEffect(borderRadius) {
       currentRadius = borderRadius
       println("🌈 Border radius changed to: $borderRadius")
   }
   LaunchedEffect(borderWidth) {
       currentWidth = borderWidth
       println("🌈 Border width changed to: $borderWidth")
   }
   ```

3. **Added Debug Logging in drawWithCache:**
   ```kotlin
   .drawWithCache {
       println("🎨 drawWithCache executing - radius: $currentRadius, width: $currentWidth")
       val strokeWidth = currentWidth.dp.toPx()
       val radius = currentRadius.dp.toPx()
       println("✏️ Calculated - strokeWidth: $strokeWidth, radius: $radius")
       // ...
   }
   ```

## How to Test & Debug

### Step 1: Build and Run

```bash
./gradlew clean
./gradlew installDebug
```

### Step 2: Open Logcat

In Android Studio:
1. Click **Logcat** tab at bottom
2. Select your device
3. Filter by: `package:mine`
4. Look for emoji: 🌈 🎨 ✏️

### Step 3: Test Slider Changes

**Action:** Move the "Corner Radius" slider

**Expected Logcat Output:**
```
🌈 Border radius changed to: 35.0
🎨 drawWithCache executing - radius: 35.0, width: 4.0
✏️ Calculated - strokeWidth: 16.0, radius: 140.0
```

**What This Means:**
- 🌈 = Parameter received from ViewModel
- 🎨 = drawWithCache re-executed  
- ✏️ = New values calculated

### Step 4: Diagnose Issues

#### Scenario A: No Logs At All

**Symptoms:** Move slider, nothing in Logcat

**Possible Causes:**
1. StateFlow not emitting
2. ViewModel not updating
3. Slider not calling callback

**Debug Steps:**
```kotlin
// In HomeScreen.kt, add to slider:
Slider(
    value = borderSettings.borderRadius,
    onValueChange = { 
        println("📊 SLIDER: value changed to $it")
        onRadiusChange(it)
    }
)

// In HomeViewModel.kt, add to update method:
fun updateBorderRadius(radius: Float) {
    println("📊 VIEWMODEL: updateBorderRadius called with $radius")
    _borderSettings.update { it.copy(borderRadius = radius) }
    println("📊 VIEWMODEL: _borderSettings updated to ${_borderSettings.value}")
}
```

#### Scenario B: 🌈 Logs But No 🎨 Logs

**Symptoms:** See "Border radius changed to: X" but no "drawWithCache executing"

**Diagnosis:** `drawWithCache` not observing `currentRadius` state properly

**Solution:** The state variable needs to be read INSIDE drawWithCache to create a snapshot dependency. This should already be the case with current code.

#### Scenario C: 🎨 Logs But No Visual Change

**Symptoms:** See both logs, but border doesn't update on screen

**Diagnosis:** Drawing code issue, not state issue

**Debug:** Check if `onDrawBehind` is actually using the new values:
```kotlin
onDrawBehind {
    println("🖌️ DRAWING: Using radius=$radius, strokeWidth=$strokeWidth")
    // ... draw operations
}
```

#### Scenario D: Logs Appear After Closing/Opening App

**Symptoms:** Move slider → no logs → close app → open app → logs appear

**Diagnosis:** Composable not recomposing on StateFlow changes

**Solution:** Check NavigationGraph.kt:
```kotlin
// Must use collectAsStateWithLifecycle, not collectAsState
val borderSettings by homeViewModel.borderSettings.collectAsStateWithLifecycle()
```

### Step 5: Verify Data Flow

Add complete logging chain:

**1. In HomeScreen.kt (Slider):**
```kotlin
onValueChange = { value ->
    println("1️⃣ SLIDER onChange: $value")
    onRadiusChange(value)
}
```

**2. In HomeViewModel.kt:**
```kotlin
fun updateBorderRadius(radius: Float) {
    println("2️⃣ VIEWMODEL update: $radius")
    _borderSettings.update { it.copy(borderRadius = radius) }
    println("3️⃣ VIEWMODEL StateFlow value: ${_borderSettings.value.borderRadius}")
}
```

**3. In NavigationGraph.kt:**
```kotlin
val borderSettings by homeViewModel.borderSettings.collectAsStateWithLifecycle()
LaunchedEffect(borderSettings.borderRadius) {
    println("4️⃣ NAVGRAPH received: ${borderSettings.borderRadius}")
}

AnimatedRainbowBorder(
    borderRadius = borderSettings.borderRadius.also { 
        println("5️⃣ PASSING to AnimatedRainbowBorder: $it") 
    }
)
```

**4. In AnimatedRainbowBorder.kt:**
```kotlin
LaunchedEffect(borderRadius) {
    println("6️⃣ RECEIVED parameter: $borderRadius")
    currentRadius = borderRadius
    println("7️⃣ SET currentRadius state: $currentRadius")
}

.drawWithCache {
    println("8️⃣ DRAW CACHE: currentRadius=$currentRadius")
    // ...
}
```

**Expected Complete Flow:**
```
1️⃣ SLIDER onChange: 35.0
2️⃣ VIEWMODEL update: 35.0
3️⃣ VIEWMODEL StateFlow value: 35.0
4️⃣ NAVGRAPH received: 35.0
5️⃣ PASSING to AnimatedRainbowBorder: 35.0
6️⃣ RECEIVED parameter: 35.0
7️⃣ SET currentRadius state: 35.0
8️⃣ DRAW CACHE: currentRadius=35.0
✏️ Calculated - strokeWidth: X, radius: 140.0
```

## Common Issues & Fixes

### Issue 1: StateFlow Not Emitting

**Check:**
```kotlin
// In HomeViewModel.kt
private val _borderSettings = MutableStateFlow(BorderSetting())

// Should emit on every update:
fun updateBorderRadius(radius: Float) {
    _borderSettings.update { it.copy(borderRadius = radius) }  // ✅ Correct
    
    // NOT:
    // _borderSettings.value.borderRadius = radius  // ❌ Wrong - doesn't emit
}
```

### Issue 2: collectAsState vs collectAsStateWithLifecycle

**Check NavigationGraph.kt:**
```kotlin
// ✅ Correct:
val borderSettings by homeViewModel.borderSettings.collectAsStateWithLifecycle()

// ❌ Wrong (might not update):
val borderSettings by homeViewModel.borderSettings.collectAsState()
```

### Issue 3: Remember Scope Issues

**The current approach:**
```kotlin
var currentRadius by remember { mutableStateOf(borderRadius) }
```

This creates state that:
- ✅ Survives recomposition
- ✅ Triggers recomposition when changed
- ✅ Can be observed by drawWithCache

### Issue 4: drawWithCache Not Observing

**Ensure state is READ inside drawWithCache:**
```kotlin
.drawWithCache {
    // ✅ Reads currentRadius - creates snapshot dependency
    val radius = currentRadius.dp.toPx()
    
    // ❌ Would not work - using parameter instead of state
    // val radius = borderRadius.dp.toPx()
}
```

## Testing Checklist

When you move a slider, check ALL these conditions:

- [ ] Logcat shows 🌈 emoji (parameter received)
- [ ] Logcat shows 🎨 emoji (drawWithCache executed)
- [ ] Logcat shows ✏️ emoji (values calculated)
- [ ] Border visually changes on screen
- [ ] Change happens immediately (< 1 second)
- [ ] No app restart needed
- [ ] Animation continues smoothly during change

## If Still Not Working

### Last Resort: Manual Invalidation

If state observation isn't working, try manual invalidation:

```kotlin
var invalidationTrigger by remember { mutableStateOf(0) }

LaunchedEffect(borderRadius) {
    currentRadius = borderRadius
    invalidationTrigger++ // Force re-execution
}

.drawWithCache {
    val trigger = invalidationTrigger // Read it to observe
    val radius = currentRadius.dp.toPx()
    // ...
}
```

### Nuclear Option: Remove drawWithCache

If drawWithCache continues to have issues:

```kotlin
// Replace with direct Canvas:
Canvas(
    modifier = Modifier.fillMaxSize()
) {
    // Read state directly
    val strokeWidth = currentWidth.dp.toPx()
    val radius = currentRadius.dp.toPx()
    val animProgress = animationProgress
    
    // ... all drawing code directly here
}
```

This is less optimal but will definitely trigger redraws.

## Expected Behavior

### ✅ Success Looks Like:

1. Move slider
2. Logcat shows: 🌈 → 🎨 → ✏️
3. Border updates on screen instantly
4. Animation continues
5. No lag, no freeze

### ❌ Failure Looks Like:

1. Move slider
2. No logs OR partial logs
3. Border stays the same
4. Need to restart to see changes

## Next Steps

1. **Run the app**
2. **Open Logcat**
3. **Move a slider**
4. **Share the Logcat output** with me

The logs will tell us EXACTLY where the issue is:
- No 🌈 = StateFlow/ViewModel issue
- 🌈 but no 🎨 = Compose observation issue  
- 🎨 but no visual = Drawing issue

Let's find out what's happening! 🔍

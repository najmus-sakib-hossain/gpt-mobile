# Chat Input Bar with FAB Transformation

## Overview

This implementation provides a smooth, animated transition between a full-featured input bar and a floating action button (FAB) based on the user's scroll position.

## Architecture

### Component Structure

```
HomeScreen.kt
├── AnimatedInputFab (Main Container)
│   ├── FullInputBar (Visible at bottom)
│   │   ├── ModelSelectorButton
│   │   ├── Tools & Options Menu
│   │   ├── @Mention Button
│   │   ├── Smart Text Input
│   │   └── GesturalActionButton
│   └── FAB (Visible when scrolled)
│       └── SpeedDialMenu (Expands on tap)
│           ├── Start Live AI
│           ├── Send Voice Memo
│           ├── New Text Prompt
│           └── Add Media
```

## Key Features

### 1. **Full Input Bar (Bottom State)**

**Location**: `ChatInputBar.kt` - `FullInputBar` composable

**Components** (Left to Right):

- **Model Selector Button**: Shows current AI model, tap to change
- **Tools & Options**: Dropdown menu for settings and Live AI toggle
- **@Mention/Collaborator**: Quick access to mentions
- **Smart Text Input**: Multi-line text field with:
  - Internal + icon for attachments
  - Internal 🎙️ icon for voice input
  - Placeholder text
  - Rounded corners, adaptive height
- **Gestural Action Button**: Context-aware button that switches between:
  - Send (➤) when text is present
  - Voice Memo (🎙️) when empty
  - Live AI (✨) when in Live AI mode

**When Visible**: User is at the bottom of the content (scroll position detected)

### 2. **Floating Action Button (Scrolled State)**

**Location**: `AnimatedInputFab.kt`

**Appearance**:

- Circular button with 💬 chat bubble icon
- Positioned in bottom-right corner
- Drop shadow for elevation
- Fixed position during scroll

**When Visible**: User has scrolled up from the bottom

### 3. **Speed Dial Menu**

**Location**: `ChatInputBar.kt` - `SpeedDialMenu` composable

**Behavior**:

- Triggers when FAB is tapped
- Semi-transparent dark overlay (scrim) appears
- Four action buttons expand vertically from FAB position
- Each button shows icon + label

**Menu Options**:

1. **✨ Start Live AI** - Launches live AI interaction
2. **🎙️ Send Voice Memo** - Records voice message
3. **✍️ New Text Prompt** - Opens text input for new chat
4. **🖼️ Add Media** - Attach images/files

## Animation Details

### Morphing Transition (Full Bar ↔ FAB)

**Location**: `AnimatedInputFab.kt`

**Full Bar to FAB** (User scrolls up):

```kotlin
slideOutVertically + fadeOut
Duration: 300ms
```

**FAB to Full Bar** (User scrolls to bottom):

```kotlin
slideInVertically + fadeIn
Spring animation with medium bounce
Duration: ~400ms
```

**State Detection**:

```kotlin
fun rememberIsAtBottom(listState: LazyListState): Boolean
```

- Checks if last item is visible
- Verifies item is fully in viewport
- Updates state automatically on scroll

### Speed Dial Expansion

**Opening Animation**:

- Scrim: fadeIn (200ms)
- Menu items: expandVertically + fadeIn with spring animation
- Staggered appearance effect

**Closing Animation**:

- Menu items: shrinkVertically + fadeOut (200ms)
- Scrim: fadeOut (200ms)

## User Interaction Flow

### Scenario 1: New Text Chat from Bottom

1. User is at bottom → Full Input Bar visible
2. User types message in text field
3. Action button shows Send icon
4. User taps Send → Message sent, navigates to chat

### Scenario 2: New Chat from Scrolled Position

1. User scrolls up → FAB appears
2. User taps FAB → Speed Dial menu expands
3. User taps "New Text Prompt"
4. App scrolls to bottom
5. Full Input Bar appears with keyboard active
6. Cursor focused in text input

### Scenario 3: Quick Voice Memo

1. User scrolls up → FAB appears
2. User taps FAB → Speed Dial menu expands
3. User taps "Send Voice Memo"
4. Voice recording interface opens
5. After recording, scrolls to bottom with message

## Implementation Details

### State Management

**Input Bar State Enum**:

```kotlin
enum class InputBarState {
    FULL_BAR,      // Full input bar at bottom
    FAB,           // Floating action button
    SPEED_DIAL     // Speed dial menu expanded
}
```

**Key State Variables**:

- `chatInput: String` - Current text input
- `isAtBottom: Boolean` - Derived from LazyListState
- `currentState: InputBarState` - Current UI state
- `showSpeedDial: Boolean` - Speed dial visibility

### Scroll Detection

```kotlin
val isAtBottom = rememberIsAtBottom(listState)
```

**Logic**:

- Monitors LazyColumn scroll position
- Returns true when last item is fully visible
- Triggers state change between FULL_BAR and FAB

### Integration Points

**HomeScreen.kt Changes**:

1. Added `rememberLazyListState()` to track scroll
2. Replaced `floatingActionButton` with `AnimatedInputFab`
3. Passed `listState` to `HomeContent`
4. Added `chatInput` state management
5. Increased bottom padding on LazyColumn (100.dp) to account for input bar

## Customization Options

### Styling

All components respect Material3 theme:

- `MaterialTheme.colorScheme.primary` - Primary actions
- `MaterialTheme.colorScheme.surface` - Surfaces
- `MaterialTheme.typography.*` - Text styles

### Timing Adjustments

Modify animation durations in:

- `AnimatedInputFab.kt` - Main transitions
- `ChatInputBar.kt` - Speed dial timing

### Add/Remove Speed Dial Items

Edit `SpeedDialMenu` in `ChatInputBar.kt`:

```kotlin
SpeedDialMenuItem(
    icon = Icons.Default.YourIcon,
    label = "Your Action",
    onClick = { /* handler */ }
)
```

## Best Practices

### Performance

- Animations use hardware acceleration
- Lazy composition of menu items
- Efficient state derivation for scroll position

### Accessibility

- All buttons have contentDescription
- Touch targets meet minimum size (48.dp)
- Clear visual feedback on interactions

### UX Polish

- Spring animations for natural feel
- Appropriate elevation/shadows
- Smooth state transitions
- Context-aware action button

## Future Enhancements

1. **Haptic Feedback**: Add vibration on FAB tap and menu selection
2. **Swipe Gestures**: Swipe up on FAB to reveal speed dial
3. **Long Press**: Long press input bar to access advanced options
4. **Voice Animation**: Pulsing animation during voice recording
5. **Smart Suggestions**: Show quick reply chips above input bar
6. **Multi-Modal Input**: Image preview in input bar
7. **Collaborative Features**: Real-time typing indicators

## Troubleshooting

### Issue: FAB doesn't appear when scrolling

- Check `listState` is properly passed to `HomeContent`
- Verify `rememberIsAtBottom` logic
- Ensure LazyColumn has sufficient items to scroll

### Issue: Animation is choppy

- Reduce animation duration
- Check for heavy operations during animation
- Profile with Layout Inspector

### Issue: Speed dial doesn't close

- Verify `showSpeedDial` state management
- Check scrim onClick handler
- Ensure proper state reset on navigation

## Files Modified/Created

### New Files

1. `ChatInputBar.kt` - Input bar and speed dial components
2. `AnimatedInputFab.kt` - Animation container and state management
3. `CHAT_INPUT_IMPLEMENTATION.md` - This documentation

### Modified Files

1. `HomeScreen.kt` - Integration of new input system

## Testing Checklist

- [ ] Full input bar appears at bottom
- [ ] FAB appears when scrolling up
- [ ] Smooth transition between states
- [ ] Speed dial menu opens/closes correctly
- [ ] All speed dial actions work
- [ ] Text input accepts keyboard input
- [ ] Send button activates with text
- [ ] Model selector shows current model
- [ ] Scrim overlay blocks content interaction
- [ ] Navigation to new chat works
- [ ] Scroll position maintained correctly
- [ ] Works in portrait and landscape
- [ ] Respects system insets (notch, navigation bar)

## Conclusion

This implementation provides a modern, polished chat input experience that maximizes screen real estate while keeping primary actions always accessible. The smooth animations and thoughtful UX patterns create a premium feel that enhances the overall app experience.

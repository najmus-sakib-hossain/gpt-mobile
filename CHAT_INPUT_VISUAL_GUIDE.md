# Chat Input Bar - Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                     HOME SCREEN (At Bottom)                     │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Content Area                            │ │
│  │                                                           │ │
│  │  • Offline AI Section                                    │ │
│  │  • Example Models                                        │ │
│  │  • Recent Chats                                          │ │
│  │                                                           │ │
│  │                [User is at bottom]                        │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│  ┃              FULL INPUT BAR (Expanded)                   ┃ │
│  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ │
│  ┃ [SmolLM▾] [⋮] [@] ┌─────────────────────┐ [➤]          ┃ │
│  ┃                    │ Type a message...   │               ┃ │
│  ┃                    │ [+]            [🎙️] │               ┃ │
│  ┃                    └─────────────────────┘               ┃ │
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↕
                     [User scrolls up]
                              ↕

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                   HOME SCREEN (Scrolled Up)                     │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Content Area                            │ │
│  │                                                           │ │
│  │  • Offline AI Section                                    │ │
│  │  • Example Models                                        │ │
│  │  • Recent Chats                                          │ │
│  │                                                           │ │
│  │                [User scrolling]                           │ │
│  │                                                           │ │
│  │                                                           │ │
│  │                                                           │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│                                                      ┏━━━━━━┓  │
│                                                      ┃  💬  ┃  │
│                                                      ┃ FAB  ┃  │
│                                                      ┗━━━━━━┛  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

                              ↕
                      [User taps FAB]
                              ↕

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│              HOME SCREEN (Speed Dial Expanded)                  │
│                                                                 │
│  ╔═══════════════════════════════════════════════════════════╗ │
│  ║           DARK OVERLAY (Semi-transparent)                 ║ │
│  ║  ┌───────────────────────────────────────────────────────┐║ │
│  ║  │                   Content Area                        │║ │
│  ║  │  (Dimmed)                                             │║ │
│  ║  │                                                       │║ │
│  ║  └───────────────────────────────────────────────────────┘║ │
│  ║                                                           ║ │
│  ║                                    ┌──────────────────┐  ║ │
│  ║                                    │ ✨ Start Live AI │  ║ │
│  ║                                    └──────────────────┘  ║ │
│  ║                                    ┌──────────────────┐  ║ │
│  ║                                    │ 🎙️ Voice Memo   │  ║ │
│  ║                                    └──────────────────┘  ║ │
│  ║                                    ┌──────────────────┐  ║ │
│  ║                                    │ ✍️ New Text      │  ║ │
│  ║                                    └──────────────────┘  ║ │
│  ║                                    ┌──────────────────┐  ║ │
│  ║                                    │ 🖼️ Add Media     │  ║ │
│  ║                                    └──────────────────┘  ║ │
│  ║                                                           ║ │
│  ║                                             ┏━━━━━━┓     ║ │
│  ║                                             ┃  💬  ┃     ║ │
│  ║                                             ┃ FAB  ┃     ║ │
│  ║                                             ┗━━━━━━┛     ║ │
│  ╚═══════════════════════════════════════════════════════════╝ │
└─────────────────────────────────────────────────────────────────┘

                              ↕
                [User taps "New Text Prompt"]
                              ↕

┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│              HOME SCREEN (Scrolled to Bottom)                   │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                   Content Area                            │ │
│  │                                                           │ │
│  │  [Auto-scrolled to bottom]                               │ │
│  │                                                           │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓ │
│  ┃              FULL INPUT BAR (Active)                     ┃ │
│  ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ │
│  ┃ [SmolLM▾] [⋮] [@] ┌─────────────────────┐ [➤]          ┃ │
│  ┃                    │ |← Cursor active    │               ┃ │
│  ┃                    │ [+]            [🎙️] │               ┃ │
│  ┃                    └─────────────────────┘               ┃ │
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛ │
│                                                                 │
│  [Keyboard appears, ready for input]                            │
└─────────────────────────────────────────────────────────────────┘
```

## Animation Timeline

```
STATE 1: FULL INPUT BAR
         ↓
    [User scrolls up]
         ↓
    ┌────────────────────────────────────┐
    │  0ms: Scroll detected              │
    │ 50ms: Input components fade out    │
    │100ms: Bar starts sliding down      │
    │200ms: Bar morphing to circle       │
    │300ms: FAB fully formed             │
    └────────────────────────────────────┘
         ↓
STATE 2: FAB
         ↓
    [User taps FAB]
         ↓
    ┌────────────────────────────────────┐
    │  0ms: Tap registered               │
    │ 50ms: Scrim fades in               │
    │100ms: First menu item appears      │
    │150ms: Second menu item appears     │
    │200ms: Third menu item appears      │
    │250ms: Fourth menu item appears     │
    └────────────────────────────────────┘
         ↓
STATE 3: SPEED DIAL OPEN
         ↓
    [User taps menu option]
         ↓
    ┌────────────────────────────────────┐
    │  0ms: Option selected              │
    │100ms: Menu items fade out          │
    │200ms: Scrim fades out              │
    │300ms: Auto-scroll to bottom starts │
    │600ms: Reached bottom               │
    │700ms: FAB morphing to bar          │
    │800ms: Input bar fully expanded     │
    │900ms: Keyboard appears             │
    └────────────────────────────────────┘
         ↓
STATE 1: FULL INPUT BAR (Active)
```

## Component Breakdown

### Full Input Bar Components

```
┌─────────────────────────────────────────────────────────────────┐
│ [1] [2] [3] [────────────4────────────] [5]                    │
└─────────────────────────────────────────────────────────────────┘

[1] Model Selector Button
    • Current model name (e.g., "SmolLM")
    • Dropdown arrow indicator
    • Tappable to change model
    • Primary container background

[2] Tools & Options Menu
    • Three-dot vertical icon
    • Dropdown menu:
      - Settings
      - Live AI toggle
    • Secondary color

[3] @Mention/Collaborator Button
    • Person icon
    • Quick access to mentions
    • Future: collaborator features

[4] Smart Text Input Box
    • Multi-line text field
    • Rounded corners (24dp)
    • Surface variant background
    • Leading icon: + (for attachments)
    • Trailing icon: 🎙️ (for voice)
    • Placeholder: "Type a message..."
    • Adaptive height (max 4 lines)

[5] Gestural Action Button
    • Context-aware icon:
      ➤ Send (when text present)
      🎙️ Voice (when empty)
      ✨ Live AI (when in Live AI mode)
    • Primary background
    • Rounded square (12dp corners)
    • 48dp × 48dp size
```

### FAB Design

```
        ┏━━━━━━━┓
        ┃       ┃
        ┃  💬   ┃  ← Chat bubble icon
        ┃       ┃
        ┗━━━━━━━┛

    • Perfect circle
    • 64dp diameter
    • Primary color background
    • Drop shadow (8dp elevation)
    • Bottom-right position (16dp margins)
    • On-tap: slight pulse effect
```

### Speed Dial Menu Items

```
┌─────────────────────────────────┐
│  ┏━━━┓  Start Live AI          │
│  ┃ ✨ ┃                         │
│  ┗━━━┛                          │
└─────────────────────────────────┘
     ↑          ↑
   Icon      Label
  (40dp)   (titleMedium)

• Each item: rounded pill shape (28dp)
• Icon in circular container (40dp)
• Clear label next to icon
• Surface background
• 4dp shadow
• Vertical stacking (12dp spacing)
```

## Interaction States

### Text Input States

```
Empty State:
┌─────────────────────┐
│ Type a message...   │  ← Placeholder visible
│ [+]            [🎙️] │
└─────────────────────┘
Action Button: 🎙️ (Voice)

Typing State:
┌─────────────────────┐
│ Hello, how are|     │  ← Text visible
│ [+]            [🎙️] │
└─────────────────────┘
Action Button: ➤ (Send)

Multi-line State:
┌─────────────────────┐
│ This is a longer    │
│ message that spans  │
│ multiple lines...   │
│ [+]            [🎙️] │
└─────────────────────┘
Action Button: ➤ (Send)
```

## Accessibility Features

```
Component               | Content Description
------------------------|------------------------------------
Model Selector          | "Current model: SmolLM, tap to change"
Tools Menu             | "Tools and options"
@Mention Button        | "Mention collaborator"
Text Input             | "Type a message"
Add Attachment         | "Add attachment"
Voice Input (internal) | "Voice input"
Action Button (Send)   | "Send message"
Action Button (Voice)  | "Record voice memo"
FAB                    | "New chat"
Speed Dial Items       | "Start Live AI", etc.
Scrim Overlay          | "Tap to close menu"
```

## Color Scheme (Material 3)

```
Component              | Color Role
-----------------------|---------------------------
Model Selector BG      | primaryContainer
Model Selector Text    | onPrimaryContainer
Tools Icon            | primary
@Mention Icon         | secondary
Text Input BG         | surfaceVariant
Text Input Text       | onSurface
Action Button BG      | primary
Action Button Icon    | onPrimary
FAB BG                | primary
FAB Icon              | onPrimary
Speed Dial Item BG    | surface
Speed Dial Icon BG    | primaryContainer
Speed Dial Icon       | onPrimaryContainer
Speed Dial Text       | onSurface
Scrim                 | Black @ 40% opacity
```

# Redesigned Input Bar - Visual Guide

## Complete Layout

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        FULL INPUT BAR (Default)                          │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [G]  [🔍]  [📋]  [👤]  ┌──────────────────────┐  [▶️]                │
│                          │ 🎤  Chat        ➕  │                        │
│                          └──────────────────────┘                        │
│   ↑    ↑     ↑     ↑              ↑               ↑                     │
│   1    2     3     4              5               6                     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘

1. Provider Logo (Google/OpenAI/etc) - Opens platform selector
2. Search Type (Search/Deep/Fast/Image/Video) - Opens dropdown
3. Context (Chat/Project/Workspace) - Opens dropdown
4. Avatar (Unsplash image) - Add people to chat
5. Text Input (Rounded BG, Mic + Plus icons)
6. Swipeable Action Button (Live AI default, swipe to change)
```

## Component Breakdown

### 1. Provider Logo Button

```
┌────┐
│ G  │  ← Google icon (or provider-specific)
└────┘
Size: 40dp
Action: Click → Platform selector dialog
Dynamic icon based on selected provider
```

### 2. Search/Response Type

```
┌────┐
│ 🔍 │  ← Search icon
└────┘
Size: 40dp
Action: Click → Dropdown menu
Options:
  • Search
  • Deep Search
  • Fast
  • Image
  • Video
```

### 3. Context Selector

```
┌────┐
│ 📋 │  ← List icon
└────┘
Size: 40dp
Action: Click → Dropdown menu
Options:
  • Chat (default)
  • Project
  • Workspace
```

### 4. Avatar Button

```
┌────┐
│ 👤 │  ← Random portrait from Unsplash
└────┘
Size: 40dp (circular)
Source: https://source.unsplash.com/random/100x100/?portrait
Action: Click → Add people modal
Fallback: Hamburger icon
```

### 5. Text Input Box (Only component with BG)

```
┌─────────────────────────┐
│ 🎤  Chat           ➕   │  ← Surface variant BG, rounded 24dp
└─────────────────────────┘
Placeholder: "Chat"
Leading: Mic icon (voice input)
Trailing: Plus icon (attachments)
Max lines: 4
Expandable
```

### 6. Swipeable Action Button

```
┌──────┐
│  ▶️  │  ← Live AI mode (default)
└──────┘
Size: 48dp
Rounded: 12dp
Swipe → : Next mode
Swipe ← : Previous mode

Modes:
  0. Live AI  (▶️) - Tertiary color
  1. Send     (➤) - Primary color
  2. Voice    (🎙️) - Secondary color
```

## Swipe Gesture Interaction

```
Live AI Mode (Default)
┌──────┐
│  ▶️  │  Swipe Right →
└──────┘
    ↓
┌──────┐
│  ➤  │  Send Mode
└──────┘
    ↓
┌──────┐
│  🎙️  │  Voice Mode
└──────┘
    ↓
┌──────┐
│  ▶️  │  Back to Live AI
└──────┘

Swipe threshold: 50px
Visual feedback: Color changes
Tertiary → Primary → Secondary → Tertiary
```

## Dropdown Menu Examples

### Search Type Menu

```
┌─────────────────┐
│ Search          │
│ Deep Search     │
│ Fast            │
│ Image           │
│ Video           │
└─────────────────┘
```

### Context Menu

```
┌─────────────────┐
│ Chat            │ ✓ (selected)
│ Project         │
│ Workspace       │
└─────────────────┘
```

## State Transitions

### Scroll State

```
At Bottom (Default):
┌────────────────────────────────────┐
│  [G] [🔍] [📋] [👤] [Input] [▶️] │  ← Full Input Bar
└────────────────────────────────────┘

        ↕ User scrolls up

Scrolled Up:
                                 ┌──┐
                                 │💬│  ← FAB
                                 └──┘

        ↕ User scrolls to bottom

Back to Full Input Bar
```

## Color Scheme by Mode

### Action Button Colors

```
Live AI Mode:
┌──────┐
│  ▶️  │  Tertiary
└──────┘

Send Mode:
┌──────┐
│  ➤  │  Primary
└──────┘

Voice Mode:
┌──────┐
│  🎙️  │  Secondary
└──────┘
```

## Provider Icon Mapping

```
Provider        | Icon
----------------|-------------
Google          | Search 🔍
OpenAI          | Face 🤖
Anthropic       | Person 👤
Offline AI      | Face 🤖
Gemini          | Search 🔍
```

## Spacing & Layout

```
Component Layout:
┌─┬──┬─┬──┬─┬──┬─┬──┬─┬────────────────┬─┬──────┬─┐
│G││🔍││📋││👤││   Input Box    ││ ▶️  ││
└─┴──┴─┴──┴─┴──┴─┴──┴─┴────────────────┴─┴──────┴─┘
 40  8  40  8  40  8  40  8    Flex      8   48

Total horizontal padding: 12dp left + 12dp right
Vertical padding: 8dp top + 8dp bottom
Spacing between components: 8dp
```

## Comparison: Old vs New

### Old Design

```
┌──────────────────────────────────────────────────────┐
│ [SmolLM▾] [⋮] [@] ┌───────────┐ [➤]                │
│                    │ Type msg  │                    │
│                    │ [+]  [🎙️] │                    │
│                    └───────────┘                    │
└──────────────────────────────────────────────────────┘
```

### New Design

```
┌──────────────────────────────────────────────────────┐
│ [G] [🔍] [📋] [👤] ┌───────────┐ [▶️]              │
│                     │ 🎤 Chat ➕ │                   │
│                     └───────────┘                   │
└──────────────────────────────────────────────────────┘
```

**Key Differences:**

- Provider logo instead of model name
- Search type selector added
- Context selector added
- Avatar for collaboration
- Only input has background (cleaner)
- Swipeable action button
- Icons positioned inside input
- "Chat" placeholder (shorter)

## User Flow Examples

### Example 1: Quick Google Search

```
1. [G] Click → Select Google
2. [🔍] Click → Select "Fast"
3. Type: "weather today"
4. [▶️] Swipe right → Send mode
5. [➤] Tap → Send query
```

### Example 2: Deep Research

```
1. [G] Already on OpenAI
2. [🔍] Click → Select "Deep Search"
3. [📋] Click → Select "Project"
4. Type: "Analyze codebase architecture"
5. [▶️] Swipe right → Send mode
6. [➤] Tap → Send query
```

### Example 3: Voice Memo

```
1. [🎤] Tap mic in input
2. Speak message
3. [▶️] Swipe right twice → Voice mode
4. [🎙️] Tap → Send voice memo
```

### Example 4: Collaborative Chat

```
1. [👤] Tap avatar
2. Select people to add
3. [📋] Click → "Workspace"
4. Type message
5. [➤] Send to team
```

## Animation Timing

```
Full Bar → FAB:
  0ms: Scroll detected
 300ms: Components fade out
 300ms: Slide down animation
 600ms: FAB appears

FAB → Full Bar:
  0ms: Scroll to bottom
 400ms: FAB morphs
 400ms: Components slide up
 700ms: Full bar visible

Swipe Gesture:
  0ms: Drag start
  50px: Mode changes
 200ms: Color transitions
```

## Accessibility

```
Component           | Content Description
--------------------|--------------------------------
Provider Logo       | "Provider: Google"
Search Type         | "Search Type"
Context Selector    | "Context"
Avatar             | "Add People"
Text Input         | "Chat"
Mic Icon           | "Voice Input"
Plus Icon          | "Add Attachment"
Action Button      | "Live AI" / "Send" / "Voice"
```

---

**Design Status**: ✅ Fully Implemented
**All Components**: ✅ Working
**Animations**: ✅ Smooth
**Gestures**: ✅ Responsive

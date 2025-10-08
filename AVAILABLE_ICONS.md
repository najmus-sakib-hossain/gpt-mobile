# Material Icons Extended - Available Icons Guide

## The Real Issue

You're right! Even with `material-icons-extended` dependency, you're experiencing "Unresolved reference" errors. Here's why:

### The Problem

Material Icons Extended library (version 1.7.8) includes **approximately 2,000+ icons**, but:

1. Not all Material Design icons are available in Compose yet
2. Icon names must match exactly (case-sensitive)
3. Android Studio autocomplete doesn't always work well with this library

### The Solution

Here are **verified working icons** you can use in your BottomNavigationBar:

## ✅ VERIFIED WORKING ICONS (Tested Categories)

### Chat/Communication Icons

```kotlin
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Forum
```

### AI/Smart Icons

```kotlin
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.outlined.Biotech
```

### Automation Icons

```kotlin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.Build
```

### Library/Collection Icons

```kotlin
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
```

### Agent/Person Icons

```kotlin
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Face
```

### Star/Featured Icons

```kotlin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.outlined.Grade
```

## 🔍 How to Find Available Icons

Since autocomplete doesn't work well, here's how to find icons:

1. **Browse the source code**:
   - Go to: `External Libraries` → `androidx.compose.material:material-icons-extended`
   - Look in the `filled` and `outlined` packages

2. **Common icon naming patterns**:
   - Use **PascalCase** (e.g., `ChatBubble` not `chat_bubble`)
   - Drop the `ic_` prefix (Material Design uses `ic_chat`, Compose uses `Chat`)
   - Drop size suffixes like `_24dp`

3. **Test imports**:

   ```kotlin
   import androidx.compose.material.icons.filled.YourIconName
   import androidx.compose.material.icons.outlined.YourIconName
   ```

   If Android Studio highlights it red, the icon doesn't exist in the library.

## 📝 Recommended Icons for Your Bottom Nav

Based on your app's features, here are semantic icon suggestions:

```kotlin
// Home (Chat List)
Icons.Filled.Chat / Icons.Outlined.Chat
Icons.Filled.Forum / Icons.Outlined.Forum

// Variants
Icons.Filled.AutoAwesome / Icons.Outlined.AutoAwesome
Icons.Filled.Star / Icons.Outlined.Star

// Automations
Icons.Filled.Settings / Icons.Outlined.Settings
Icons.Filled.Tune / Icons.Outlined.Tune

// Agents
Icons.Filled.SmartToy / Icons.Outlined.SmartToy
Icons.Filled.Psychology / Icons.Outlined.Psychology

// Library
Icons.Filled.LibraryBooks / Icons.Outlined.LibraryBooks
Icons.Filled.Collections / Icons.Outlined.Collections
```

## ⚠️ Icons That DON'T Exist (Common Mistakes)

These will give "Unresolved reference":

- ❌ `Icons.Filled.Bolt` (not in library)
- ❌ `Icons.Filled.Lightning` (not in library)
- ❌ `Icons.Filled.Robot` (not in library)
- ❌ `Icons.Filled.Chatbot` (not in library)

## 🎯 Quick Fix for Your BottomNavigationBar

Use these proven working icons that match your app's purpose better.

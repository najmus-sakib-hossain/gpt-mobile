# Debugging Offline Model Loading

## Overview

The offline model loading process has been enhanced with automatic content URI to file path conversion. If a model path is stored as a `content://` URI (e.g., from the Downloads provider), it will automatically be copied to internal storage when first used.

## Added Logging Points

I've added comprehensive debug logging to trace the model path from file selection through to loading. Here's what to look for in logcat when testing:

### 1. File Selection (HomeScreen.kt)

When you select a GGUF file using the file picker:

```
D/HomeScreen: Starting to copy model file from URI: content://...
D/HomeScreen: Extracted file name: your-model.gguf
```

### 2. File Copying (FileUtil.kt)

When the file is being copied to internal storage:

```
D/FileUtil: Starting copy from URI: content://...
D/FileUtil: Target file name: your-model.gguf
D/FileUtil: Created models directory: true at /data/data/dev.chungjungsoo.gptmobile/files/models
D/FileUtil: Destination file path: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
D/FileUtil: Copied XXXXX bytes
D/FileUtil: File copied successfully to: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
D/FileUtil: File exists: true, Size: XXXXX bytes
```

If copying fails:

```
E/FileUtil: Failed to open input stream for URI: content://...
or
E/FileUtil: IOException while copying file
```

### 3. Path Update (HomeScreen.kt)

After successful copy:

```
D/HomeScreen: File copied successfully to: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
```

If copy fails:

```
E/HomeScreen: Failed to copy file from URI: content://...
```

### 4. ViewModel Update (HomeViewModel.kt)

When the model path is saved to settings:

```
D/HomeViewModel: Updating offline model path to: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
D/HomeViewModel: Platform settings saved with model path: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
```

### 5. Model Loading (ChatRepositoryImpl.kt)

When you start a chat with Offline AI:

```
D/ChatRepositoryImpl: Attempting to use offline model at: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
```

**IMPORTANT**: If the path is a content URI (e.g., `content://com.android.providers.downloads.documents/document/17`), it will be automatically converted:

```
D/ChatRepositoryImpl: Attempting to use offline model at: content://com.android.providers.downloads.documents/document/17
D/FileUtil: Model path is a content URI, converting: content://...
D/FileUtil: Starting copy from URI: content://...
D/FileUtil: Converted content URI to file path: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
D/ChatRepositoryImpl: Using model file at: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
```

If loading succeeds:

```
D/LLMServiceImpl: Loading model from: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
D/LLMServiceImpl: Model loaded successfully
D/ChatRepositoryImpl: Model loaded successfully from: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
```

If loading fails:

```
E/LLMServiceImpl: Model file does not exist: /data/data/dev.chungjungsoo.gptmobile/files/models/your-model.gguf
or
E/LLMServiceImpl: Failed to load model
```

## Testing Steps

1. **Open the app**
2. **Go to Settings → Offline AI Settings** (or use the file picker button in platform selection)
3. **Select a GGUF model file** from your device
4. **Watch logcat** for the file copying process
5. **Create a new chat** and select "Offline AI" platform
6. **Send a message** and watch for model loading logs

## Filter Logcat

Use this filter to see only relevant logs:

```
adb logcat -s HomeScreen:D FileUtil:D HomeViewModel:D ChatRepositoryImpl:D LLMServiceImpl:D
```

Or in Android Studio's logcat, use this filter:

```
tag:HomeScreen|FileUtil|HomeViewModel|ChatRepositoryImpl|LLMServiceImpl
```

## Common Issues to Check

### Issue 1: File not found

If you see `Model file does not exist`, check:

- Was the file copied successfully? (Look for FileUtil success logs)
- Is the path correct? (Should be in `/data/data/dev.chungjungsoo.gptmobile/files/models/`)
- Did the ViewModel save the path? (Look for HomeViewModel logs)

### Issue 2: File copy fails

If FileUtil logs show failure:

- Check if you have storage permissions
- Check if the source file is accessible
- Check if there's enough space in internal storage

### Issue 3: Model path not saved

If HomeViewModel doesn't log the path update:

- The file picker callback might not be firing
- Check for any exceptions in the logs

### Issue 4: Native library error

If model file exists but load fails:

- Check if libsmollm.so and libggufreader.so are in the APK
- Run: `unzip -l app-debug.apk | grep "\.so$"` to verify
- Make sure the GGUF file is a valid model format
- Check if you're on x86_64 emulator (ARM .so files not yet added)

## Expected Flow Summary

1. User selects GGUF file → **URI received**
2. FileUtil copies to internal storage → **Absolute path obtained**
3. HomeViewModel saves path to DataStore → **Path persisted**
4. User creates chat with Offline AI → **Path retrieved from platform.model**
5. LLMServiceImpl loads model → **SmolLM initialized**
6. User sends message → **Tokens streamed back**

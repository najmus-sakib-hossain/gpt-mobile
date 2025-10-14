package dev.chungjungsoo.gptmobile.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {
    /**
     * Copy a file from a content URI to app's internal storage
     * Returns the absolute file path of the copied file
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String = "offline_model.gguf"): String? {
        return try {
            android.util.Log.d("FileUtil", "Starting copy from URI: $uri")
            android.util.Log.d("FileUtil", "Target file name: $fileName")
            
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.e("FileUtil", "Failed to open input stream for URI: $uri")
                return null
            }
            
            // Create models directory in internal storage
            val modelsDir = File(context.filesDir, "models")
            if (!modelsDir.exists()) {
                val created = modelsDir.mkdirs()
                android.util.Log.d("FileUtil", "Created models directory: $created at ${modelsDir.absolutePath}")
            }
            
            // Create destination file
            val destFile = File(modelsDir, fileName)
            android.util.Log.d("FileUtil", "Destination file path: ${destFile.absolutePath}")
            
            // Copy file
            FileOutputStream(destFile).use { outputStream ->
                inputStream.use { input ->
                    val bytesCopied = input.copyTo(outputStream)
                    android.util.Log.d("FileUtil", "Copied $bytesCopied bytes")
                }
            }
            
            android.util.Log.d("FileUtil", "File copied successfully to: ${destFile.absolutePath}")
            android.util.Log.d("FileUtil", "File exists: ${destFile.exists()}, Size: ${destFile.length()} bytes")
            destFile.absolutePath
        } catch (e: IOException) {
            android.util.Log.e("FileUtil", "IOException while copying file", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get file name from URI
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = "offline_model.gguf"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        return fileName
    }
    
    /**
     * Get readable path from URI (for display purposes)
     * Returns either the file name or a shortened URI
     */
    fun getReadablePathFromUri(context: Context, uri: Uri): String {
        val fileName = getFileNameFromUri(context, uri)
        return fileName.ifEmpty { uri.lastPathSegment ?: uri.toString() }
    }
    
    /**
     * Ensure the model path is a valid file path, not a content URI.
     * If the path is a content URI, copy it to internal storage and return the file path.
     * If it's already a file path, return it as-is.
     */
    suspend fun ensureModelFileExists(context: Context, modelPath: String): String? {
        return try {
            // Check if it's a content URI
            if (modelPath.startsWith("content://")) {
                android.util.Log.d("FileUtil", "Model path is a content URI, converting: $modelPath")
                val uri = Uri.parse(modelPath)
                val fileName = getFileNameFromUri(context, uri)
                val filePath = copyUriToInternalStorage(context, uri, fileName)
                
                if (filePath != null) {
                    android.util.Log.d("FileUtil", "Converted content URI to file path: $filePath")
                } else {
                    android.util.Log.e("FileUtil", "Failed to convert content URI to file path")
                }
                filePath
            } else {
                // It's already a file path, verify it exists
                val file = File(modelPath)
                if (file.exists()) {
                    android.util.Log.d("FileUtil", "Model file exists at: $modelPath")
                    modelPath
                } else {
                    android.util.Log.e("FileUtil", "Model file does not exist at: $modelPath")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtil", "Error ensuring model file exists", e)
            null
        }
    }
}


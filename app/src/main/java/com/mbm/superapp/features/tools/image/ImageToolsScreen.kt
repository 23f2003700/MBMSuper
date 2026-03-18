package com.mbm.superapp.features.tools.image

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var quality by remember { mutableFloatStateOf(80f) }
    var scalePercent by remember { mutableFloatStateOf(100f) }
    var statusMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var lastOutputFile by remember { mutableStateOf<File?>(null) }
    var targetSizeText by remember { mutableStateOf("") }
    var targetSizeUnit by remember { mutableStateOf("KB") } // KB or MB

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedUri = uri
        if (uri != null) statusMessage = "Image selected"
        lastOutputFile = null
    }

    fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    }

    fun saveToGallery(file: File, mimeType: String) {
        scope.launch {
            val saved = saveImageToGallery(context, file, mimeType)
            if (saved) {
                Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Tools") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // Select Image
            Button(
                onClick = { imagePicker.launch(arrayOf("image/*")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Select Image")
            }

            Spacer(Modifier.height(20.dp))

            // Quality slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Compression Quality: ${quality.toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = quality,
                        onValueChange = { quality = it },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Resize slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resize: ${scalePercent.toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = scalePercent,
                        onValueChange = { scalePercent = it },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Compress as JPG
            ActionButton(
                text = "Compress as JPG",
                enabled = selectedUri != null && !isProcessing,
                onClick = {
                    selectedUri?.let {
                        isProcessing = true
                        scope.launch {
                            val result = compressImage(context, it, quality.toInt(), scalePercent / 100f, Bitmap.CompressFormat.JPEG, "jpg")
                            statusMessage = result.first
                            lastOutputFile = result.second
                            isProcessing = false
                        }
                    }
                },
            )
            Spacer(Modifier.height(8.dp))

            // Save as PNG
            ActionButton(
                text = "Save as PNG",
                enabled = selectedUri != null && !isProcessing,
                onClick = {
                    selectedUri?.let {
                        isProcessing = true
                        scope.launch {
                            val result = compressImage(context, it, 100, scalePercent / 100f, Bitmap.CompressFormat.PNG, "png")
                            statusMessage = result.first
                            lastOutputFile = result.second
                            isProcessing = false
                        }
                    }
                },
            )
            Spacer(Modifier.height(8.dp))

            // Save as WebP
            ActionButton(
                text = "Save as WebP",
                enabled = selectedUri != null && !isProcessing,
                onClick = {
                    selectedUri?.let {
                        isProcessing = true
                        scope.launch {
                            @Suppress("DEPRECATION")
                            val result = compressImage(context, it, quality.toInt(), scalePercent / 100f, Bitmap.CompressFormat.WEBP, "webp")
                            statusMessage = result.first
                            lastOutputFile = result.second
                            isProcessing = false
                        }
                    }
                },
            )

            Spacer(Modifier.height(20.dp))

            // Target file size section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Compress to Exact Size",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = targetSizeText,
                            onValueChange = { v -> targetSizeText = v.filter { it.isDigit() || it == '.' } },
                            label = { Text("Target Size") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            ),
                        )
                        Column {
                            listOf("KB", "MB").forEach { unit ->
                                Button(
                                    onClick = { targetSizeUnit = unit },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (targetSizeUnit == unit) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        contentColor = if (targetSizeUnit == unit) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary,
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(56.dp),
                                ) {
                                    Text(unit, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                val sizeVal = targetSizeText.toDoubleOrNull()
                                if (sizeVal != null && sizeVal > 0) {
                                    val targetBytes = if (targetSizeUnit == "MB") (sizeVal * 1024 * 1024).toLong() else (sizeVal * 1024).toLong()
                                    isProcessing = true
                                    scope.launch {
                                        val result = compressToTargetSize(context, uri, targetBytes, scalePercent / 100f)
                                        statusMessage = result.first
                                        lastOutputFile = result.second
                                        isProcessing = false
                                    }
                                }
                            }
                        },
                        enabled = selectedUri != null && !isProcessing && targetSizeText.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Text("Compress to Target Size")
                    }
                }
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (lastOutputFile != null) {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        lastOutputFile?.let {
                                            val mime = when (it.extension) {
                                                "png" -> "image/png"
                                                "webp" -> "image/webp"
                                                else -> "image/jpeg"
                                            }
                                            shareFile(it, mime)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share")
                                }
                                OutlinedButton(
                                    onClick = {
                                        lastOutputFile?.let {
                                            val mime = when (it.extension) {
                                                "png" -> "image/png"
                                                "webp" -> "image/webp"
                                                else -> "image/jpeg"
                                            }
                                            saveToGallery(it, mime)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Save to Gallery")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ActionButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text)
    }
}

private suspend fun saveImageToGallery(context: Context, file: File, mimeType: String): Boolean = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MBMSuper")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    file.inputStream().use { input -> input.copyTo(os) }
                }
                true
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MBMSuper")
            dir.mkdirs()
            file.copyTo(File(dir, file.name), overwrite = true)
            true
        }
    } catch (_: Exception) {
        false
    }
}

private suspend fun compressImage(
    context: Context,
    uri: Uri,
    quality: Int,
    scale: Float,
    format: Bitmap.CompressFormat,
    extension: String,
): Pair<String, File?> = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext Pair("Error: Cannot read file", null)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val newW = (original.width * scale).toInt().coerceAtLeast(1)
        val newH = (original.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)

        val outFile = File(context.cacheDir, "img_${System.currentTimeMillis()}.$extension")
        FileOutputStream(outFile).use { fos ->
            scaled.compress(format, quality, fos)
        }

        val newSize = outFile.length() / 1024
        original.recycle()
        if (scaled !== original) scaled.recycle()

        Pair("${extension.uppercase()}: ${newW}x${newH}, ${newSize}KB", outFile)
    } catch (e: Exception) {
        Pair("Error: ${e.message}", null)
    }
}

private suspend fun compressToTargetSize(
    context: Context,
    uri: Uri,
    targetBytes: Long,
    scale: Float,
): Pair<String, File?> = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext Pair("Error: Cannot read file", null)
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val newW = (original.width * scale).toInt().coerceAtLeast(1)
        val newH = (original.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)

        // Binary search for the right quality
        var low = 5
        var high = 100
        var bestFile: File? = null
        var bestSize = Long.MAX_VALUE

        while (low <= high) {
            val mid = (low + high) / 2
            val tempFile = File(context.cacheDir, "img_target_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, mid, fos)
            }
            val fileSize = tempFile.length()

            if (fileSize <= targetBytes) {
                bestFile?.delete()
                bestFile = tempFile
                bestSize = fileSize
                low = mid + 1
            } else {
                tempFile.delete()
                high = mid - 1
            }
        }

        original.recycle()
        if (scaled !== original) scaled.recycle()

        if (bestFile != null) {
            val resultKB = bestSize / 1024
            Pair("Target: ${newW}x${newH}, ${resultKB}KB (q=${low - 1})", bestFile)
        } else {
            // Even quality=5 is too big — try with additional scaling
            val smallerScale = scale * 0.5f
            val sw = (original.width * smallerScale).toInt().coerceAtLeast(1)
            val sh = (original.height * smallerScale).toInt().coerceAtLeast(1)
            val reOriginal = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            val small = Bitmap.createScaledBitmap(reOriginal, sw, sh, true)
            val outFile = File(context.cacheDir, "img_target_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outFile).use { fos ->
                small.compress(Bitmap.CompressFormat.JPEG, 30, fos)
            }
            val sz = outFile.length() / 1024
            reOriginal.recycle()
            small.recycle()
            Pair("Scaled down: ${sw}x${sh}, ${sz}KB", outFile)
        }
    } catch (e: Exception) {
        Pair("Error: ${e.message}", null)
    }
}

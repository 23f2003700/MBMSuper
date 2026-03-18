package com.mbm.superapp.features.tools.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFiles = remember { mutableStateListOf<Uri>() }
    var statusMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var lastOutputFile by remember { mutableStateOf<File?>(null) }
    var currentAction by remember { mutableStateOf("merge") }

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedFiles.clear()
        selectedFiles.addAll(uris)
        if (uris.isNotEmpty()) {
            statusMessage = "${uris.size} file(s) selected"
            // Auto-split if single PDF and action is split
            if (currentAction == "split" && uris.size == 1) {
                isProcessing = true
                scope.launch {
                    val result = splitPdf(context, uris[0])
                    statusMessage = result.first
                    lastOutputFile = result.second
                    isProcessing = false
                }
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            isProcessing = true
            scope.launch {
                val result = convertImagesToPdf(context, uris)
                statusMessage = result.first
                lastOutputFile = result.second
                isProcessing = false
            }
        }
    }

    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    fun saveToDownloads(file: File) {
        scope.launch {
            val saved = savePdfToDownloads(context, file)
            if (saved) {
                Toast.makeText(context, "Saved to Downloads!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Tools") },
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
            // Merge PDF
            PdfActionCard(
                title = "Merge PDFs",
                description = "Select multiple PDF files to combine into one",
                buttonText = "Select PDFs",
                onAction = { currentAction = "merge"; pdfPicker.launch(arrayOf("application/pdf")) },
            )

            if (currentAction == "merge" && selectedFiles.size > 1) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val result = mergePdfs(context, selectedFiles.toList())
                            statusMessage = result.first
                            lastOutputFile = result.second
                            isProcessing = false
                            selectedFiles.clear()
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (isProcessing) "Merging..." else "Merge ${selectedFiles.size} Files")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Image to PDF
            PdfActionCard(
                title = "Images to PDF",
                description = "Convert selected images to a PDF document",
                buttonText = "Select Images",
                onAction = { currentAction = "img2pdf"; imagePicker.launch(arrayOf("image/*")) },
            )

            Spacer(Modifier.height(16.dp))

            // Split PDF
            PdfActionCard(
                title = "Split PDF",
                description = "Extract individual pages as separate PDFs",
                buttonText = "Select PDF",
                onAction = { currentAction = "split"; pdfPicker.launch(arrayOf("application/pdf")) },
            )

            // Result section with share/save
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
                                    onClick = { lastOutputFile?.let { shareFile(it) } },
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
                                    onClick = { lastOutputFile?.let { saveToDownloads(it) } },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Save to Downloads")
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
private fun PdfActionCard(
    title: String,
    description: String,
    buttonText: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

private suspend fun savePdfToDownloads(context: Context, file: File): Boolean = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    file.inputStream().use { input -> input.copyTo(os) }
                }
                true
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            val dest = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file.name)
            file.copyTo(dest, overwrite = true)
            true
        }
    } catch (_: Exception) {
        false
    }
}

private suspend fun mergePdfs(context: Context, uris: List<Uri>): Pair<String, File?> = withContext(Dispatchers.IO) {
    try {
        val outDoc = PdfDocument()
        var pageNum = 0

        for (uri in uris) {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: continue
            val renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val srcPage = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(srcPage.width, srcPage.height, Bitmap.Config.ARGB_8888)
                srcPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                srcPage.close()

                pageNum++
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageNum).create()
                val dstPage = outDoc.startPage(pageInfo)
                dstPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                outDoc.finishPage(dstPage)
                bitmap.recycle()
            }
            renderer.close()
            pfd.close()
        }

        val outFile = File(context.cacheDir, "merged_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { outDoc.writeTo(it) }
        outDoc.close()
        Pair("Merged $pageNum pages!", outFile)
    } catch (e: Exception) {
        Pair("Error: ${e.message}", null)
    }
}

private suspend fun splitPdf(context: Context, uri: Uri): Pair<String, File?> = withContext(Dispatchers.IO) {
    try {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: return@withContext Pair("Error: Cannot read PDF", null)
        val renderer = PdfRenderer(pfd)
        val count = renderer.pageCount
        var lastFile: File? = null

        for (i in 0 until count) {
            val srcPage = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(srcPage.width, srcPage.height, Bitmap.Config.ARGB_8888)
            srcPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            srcPage.close()

            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = doc.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            doc.finishPage(page)
            bitmap.recycle()

            val outFile = File(context.cacheDir, "page_${i + 1}.pdf")
            FileOutputStream(outFile).use { doc.writeTo(it) }
            doc.close()

            // Save each page directly to Downloads
            savePdfToDownloads(context, outFile)
            lastFile = outFile
        }
        renderer.close()
        pfd.close()
        Pair("Split into $count pages! Saved to Downloads.", lastFile)
    } catch (e: Exception) {
        Pair("Error: ${e.message}", null)
    }
}

private suspend fun convertImagesToPdf(context: Context, imageUris: List<Uri>): Pair<String, File?> = withContext(Dispatchers.IO) {
    try {
        val doc = PdfDocument()
        var pageNum = 0

        for (uri in imageUris) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    pageNum++
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pageNum).create()
                    val page = doc.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    doc.finishPage(page)
                    bitmap.recycle()
                }
            }
        }

        val outFile = File(context.cacheDir, "images_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        Pair("PDF created with $pageNum image(s)!", outFile)
    } catch (e: Exception) {
        Pair("Error: ${e.message}", null)
    }
}

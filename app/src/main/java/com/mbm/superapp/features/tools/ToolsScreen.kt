package com.mbm.superapp.features.tools

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MergeType
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ToolsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "All processing happens on your device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(24.dp))

        // PDF Tools Section
        Text(
            text = "PDF Tools",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))

        ToolCard(
            icon = Icons.Outlined.MergeType,
            title = "Merge PDF",
            description = "Combine multiple PDF files into one",
            onClick = { navController.navigate("tools/pdf") },
        )
        Spacer(Modifier.height(8.dp))
        ToolCard(
            icon = Icons.Outlined.PictureAsPdf,
            title = "Split PDF",
            description = "Extract pages from a PDF file",
            onClick = { navController.navigate("tools/pdf") },
        )
        Spacer(Modifier.height(8.dp))
        ToolCard(
            icon = Icons.Outlined.Compress,
            title = "Compress PDF",
            description = "Reduce PDF file size",
            onClick = { navController.navigate("tools/pdf") },
        )
        Spacer(Modifier.height(8.dp))
        ToolCard(
            icon = Icons.Outlined.Image,
            title = "Image to PDF",
            description = "Convert images to PDF document",
            onClick = { navController.navigate("tools/pdf") },
        )

        Spacer(Modifier.height(28.dp))

        // Image Tools Section
        Text(
            text = "Image Tools",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))

        ToolCard(
            icon = Icons.Outlined.Compress,
            title = "Compress Image",
            description = "Reduce image file size with quality control",
            onClick = { navController.navigate("tools/image") },
        )
        Spacer(Modifier.height(8.dp))
        ToolCard(
            icon = Icons.Outlined.PhotoSizeSelectLarge,
            title = "Resize Image",
            description = "Change image dimensions",
            onClick = { navController.navigate("tools/image") },
        )
        Spacer(Modifier.height(8.dp))
        ToolCard(
            icon = Icons.Outlined.Transform,
            title = "Convert Format",
            description = "Convert between PNG, JPG, WebP",
            onClick = { navController.navigate("tools/image") },
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ToolCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

package com.mbm.superapp.features.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val MBM_LAT = 26.268047432453386
private const val MBM_LNG = 73.03580981085543

private val mapStyles = listOf("Street", "Satellite", "Terrain", "Dark")

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MBMMapScreen() {
    var selectedStyle by remember { mutableIntStateOf(1) } // default satellite
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun buildMapHtml(style: Int): String {
        val tileUrl = when (style) {
            0 -> "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
            1 -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
            2 -> "https://tile.opentopomap.org/{z}/{x}/{y}.png"
            3 -> "https://tiles.stadiamaps.com/tiles/alidade_smooth_dark/{z}/{x}/{y}{r}.png"
            else -> "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        }
        val attribution = when (style) {
            1 -> "Esri, Maxar, Earthstar Geographics"
            2 -> "OpenTopoMap"
            3 -> "Stadia Maps"
            else -> "OpenStreetMap contributors"
        }
        return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>
html,body{margin:0;padding:0;height:100%;overflow:hidden}
#map{width:100%;height:100%}
.leaflet-control-attribution{font-size:9px !important}
</style>
</head>
<body>
<div id="map"></div>
<script>
var map = L.map('map', {zoomControl: true, attributionControl: true}).setView([$MBM_LAT, $MBM_LNG], 17);
L.tileLayer('$tileUrl', {
    maxZoom: 20,
    attribution: '&copy; $attribution'
}).addTo(map);
var marker = L.marker([$MBM_LAT, $MBM_LNG]).addTo(map);
marker.bindPopup('<b>MBM University</b><br>Jodhpur, Rajasthan').openPopup();
L.circle([$MBM_LAT, $MBM_LNG], {
    color: '#4285F4', fillColor: '#4285F4', fillOpacity: 0.08, radius: 300
}).addTo(map);
function centerMap(){map.setView([$MBM_LAT,$MBM_LNG],17)}
</script>
</body>
</html>
""".trimIndent()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "MBM Campus Map",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "MBM University, Jodhpur • Satellite & Street View",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    mapStyles.forEachIndexed { index, label ->
                        AssistChip(
                            onClick = {
                                selectedStyle = index
                                isLoading = true
                                webViewRef?.loadDataWithBaseURL(
                                    "https://openstreetmap.org",
                                    buildMapHtml(index),
                                    "text/html", "UTF-8", null
                                )
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (index == selectedStyle)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                borderColor = if (index == selectedStyle)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                enabled = true,
                            ),
                        )
                        if (index < mapStyles.size - 1) Spacer(Modifier.width(6.dp))
                    }
                }
            }

            // WebView Map
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = false
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                }
                            }
                            webChromeClient = WebChromeClient()
                            webViewRef = this
                            loadDataWithBaseURL(
                                "https://openstreetmap.org",
                                buildMapHtml(selectedStyle),
                                "text/html", "UTF-8", null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Center FAB
        FloatingActionButton(
            onClick = { webViewRef?.evaluateJavascript("centerMap()", null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Outlined.MyLocation, "Center on MBM")
        }
    }
}

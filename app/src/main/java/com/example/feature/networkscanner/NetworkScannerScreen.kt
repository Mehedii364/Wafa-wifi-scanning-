package com.example.feature.networkscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.core.common.WifiBand
import com.example.core.common.WifiUtils
import com.example.core.designsystem.AnimatedRadarScanner
import com.example.core.designsystem.NeumorphicCard
import com.example.core.designsystem.SignalQualityBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NetworkScannerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scanResults by viewModel.wifiMonitor.scanResults.collectAsState()
    val isScanning by viewModel.wifiMonitor.isScanning.collectAsState()
    val scanMessage by viewModel.wifiMonitor.scanMessage.collectAsState()
    val lastScanTime by viewModel.wifiMonitor.lastScanTime.collectAsState()
    val maskPii by viewModel.maskPii.collectAsState()

    var selectedBandFilter by remember { mutableStateOf<WifiBand?>(null) }

    val filteredList = scanResults.filter {
        selectedBandFilter == null || it.band == selectedBandFilter
    }

    val lastScanStr = if (lastScanTime > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastScanTime))
    } else {
        "None"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Scanner Top Control Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.getString("scanner"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Discovered: ${scanResults.size} APs • Last scan: $lastScanStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { viewModel.toggleMaskPii() },
                modifier = Modifier.testTag("scanner_mask_toggle")
            ) {
                Icon(
                    imageVector = if (maskPii) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Masking",
                    tint = CyanAccent
                )
            }
        }

        // Radar Graphic + Scan Action Button
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "scan_action_card") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedRadarScanner(
                        isScanning = isScanning,
                        modifier = Modifier.size(52.dp)
                    )

                    Column {
                        Text(
                            text = if (isScanning) "Searching RF Spectrum..." else "Nearby Wi-Fi Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "2.4 GHz & 5 GHz bands",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { viewModel.scanNetworks() },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("trigger_scan_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isScanning) "Scanning" else "Scan", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Throttling warning banner if present
        if (!scanMessage.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x22F59E0B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ $scanMessage",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Band Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedBandFilter == null,
                onClick = { selectedBandFilter = null },
                label = { Text("All (${scanResults.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = Color.Black
                )
            )

            FilterChip(
                selected = selectedBandFilter == WifiBand.BAND_2_4_GHZ,
                onClick = { selectedBandFilter = WifiBand.BAND_2_4_GHZ },
                label = { Text("2.4 GHz") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = Color.Black
                )
            )

            FilterChip(
                selected = selectedBandFilter == WifiBand.BAND_5_GHZ,
                onClick = { selectedBandFilter = WifiBand.BAND_5_GHZ },
                label = { Text("5 GHz") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanAccent,
                    selectedLabelColor = Color.Black
                )
            )
        }

        // Scanned AP List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (scanResults.isEmpty()) "Tap 'Scan' to discover nearby Wi-Fi Access Points." else "No access points match the selected band.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { ap ->
                    val displaySsid = if (maskPii) WifiUtils.maskSsid(ap.ssid) else ap.ssid
                    val displayBssid = if (maskPii) WifiUtils.maskBssid(ap.bssid) else ap.bssid
                    val channelStr = ap.channel?.let { "Ch $it" } ?: "Ch N/A"

                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "scanned_ap_card_${ap.bssid}"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = displaySsid,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                SignalQualityBadge(rssi = ap.rssi)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$channelStr • ${ap.frequencyMhz} MHz • ${ap.band.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = displayBssid,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Security: ${ap.capabilities}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

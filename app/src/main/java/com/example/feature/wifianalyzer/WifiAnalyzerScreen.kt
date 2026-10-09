package com.example.feature.wifianalyzer

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.core.common.WifiUtils
import com.example.core.designsystem.NeumorphicCard
import com.example.core.designsystem.SignalQualityBadge
import com.example.core.designsystem.SparklineSignalChart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletAccent

@Composable
fun WifiAnalyzerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentWifi by viewModel.wifiMonitor.currentConnection.collectAsState()
    val isMonitoring by viewModel.wifiMonitor.isMonitoring.collectAsState()
    val signalTrend by viewModel.signalTrend.collectAsState()
    val intervalSec by viewModel.monitoringIntervalSec.collectAsState()
    val maskPii by viewModel.maskPii.collectAsState()

    val displaySsid = if (maskPii) WifiUtils.maskSsid(currentWifi.ssid) else currentWifi.ssid
    val isStale = System.currentTimeMillis() - currentWifi.timestamp > (intervalSec * 2500L) && currentWifi.timestamp > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Text(
            text = viewModel.getString("analyzer"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Session Control Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "analyzer_controls_card") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = displaySsid,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentWifi.isConnected) "${currentWifi.frequencyMhz} MHz • ${currentWifi.linkSpeedMbps} Mbps" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentWifi.isConnected) {
                        SignalQualityBadge(rssi = currentWifi.rssi)
                    }
                }

                if (isStale) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x33F59E0B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠ Stale reading: measurement is older than monitoring cycle.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Interval chips
                Text(
                    text = viewModel.getString("interval_sec"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5, 10).forEach { sec ->
                        FilterChip(
                            selected = intervalSec == sec,
                            onClick = { viewModel.setMonitoringInterval(sec) },
                            label = { Text("${sec}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isMonitoring) viewModel.stopMonitoring() else viewModel.startMonitoring()
                        },
                        modifier = Modifier.weight(1f).testTag("analyzer_start_stop_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMonitoring) Color(0xFFEF4444) else CyanAccent,
                            contentColor = if (isMonitoring) Color.White else Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isMonitoring) viewModel.getString("stop_monitoring") else viewModel.getString("start_monitoring"))
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportData(context, "CSV") },
                        modifier = Modifier.testTag("analyzer_export_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = VioletAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV")
                    }
                }
            }
        }

        // Live Analyzer Chart
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "analyzer_chart_card") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "High-Resolution RSSI Trajectory (dBm)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Continuous signal amplitude readings. Horizontal grid indicates -50 dBm (Excellent), -70 dBm (Fair), -90 dBm (Weak).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SparklineSignalChart(
                    dataPoints = signalTrend,
                    lineColor = CyanAccent,
                    fillAlpha = 0.25f
                )
            }
        }

        // Educational Technical Explanation: RSSI vs Speed
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "analyzer_info_card") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Technical Note: RSSI vs. Internet Speed",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "• RSSI (Received Signal Strength Indicator) quantifies RF carrier power received by device antennas, typically -30 dBm (very strong) to -90 dBm (faint).\n" +
                            "• Link Speed (PHY rate) represents the theoretical wireless negotiation rate based on channel width and MCS index, not your ISP download throughput.\n" +
                            "• Real-world speed also depends on backhaul bandwidth, channel contention, packet retransmissions, and bufferbloat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

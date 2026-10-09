package com.example.feature.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.NavScreen
import com.example.core.common.WifiUtils
import com.example.core.designsystem.NeumorphicCard
import com.example.core.designsystem.SignalQualityBadge
import com.example.core.designsystem.SparklineSignalChart
import com.example.core.designsystem.StatMetricCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentWifi by viewModel.wifiMonitor.currentConnection.collectAsState()
    val isMonitoring by viewModel.wifiMonitor.isMonitoring.collectAsState()
    val isScanning by viewModel.wifiMonitor.isScanning.collectAsState()
    val signalTrend by viewModel.signalTrend.collectAsState()
    val maskPii by viewModel.maskPii.collectAsState()
    val hardwareProfile = viewModel.hardwareProfile

    val displaySsid = if (maskPii) WifiUtils.maskSsid(currentWifi.ssid) else currentWifi.ssid
    val displayBssid = if (maskPii) WifiUtils.maskBssid(currentWifi.bssid) else currentWifi.bssid
    val channel = WifiUtils.frequencyToChannel(currentWifi.frequencyMhz)?.toString() ?: "N/A"
    val band = WifiUtils.frequencyToBand(currentWifi.frequencyMhz).displayName
    val lastUpdateStr = if (currentWifi.timestamp > 0) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentWifi.timestamp))
    } else {
        "Awaiting update"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Connection Hero Banner
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "hero_connection_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (currentWifi.isConnected) EmeraldGreen else Color.Gray)
                        )
                        Text(
                            text = if (currentWifi.isConnected) viewModel.getString("connected") else viewModel.getString("disconnected"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentWifi.isConnected) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleMaskPii() },
                        modifier = Modifier.size(36.dp).testTag("toggle_mask_button")
                    ) {
                        Icon(
                            imageVector = if (maskPii) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Masking",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = displaySsid,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "BSSID: $displayBssid • IP: ${currentWifi.ipAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentWifi.isConnected) {
                    SignalQualityBadge(rssi = currentWifi.rssi)
                }
            }
        }

        // Monitoring Session Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isMonitoring) viewModel.stopMonitoring() else viewModel.startMonitoring()
                },
                modifier = Modifier.weight(1f).testTag("monitoring_toggle_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMonitoring) Color(0xFFEF4444) else CyanAccent,
                    contentColor = if (isMonitoring) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMonitoring) viewModel.getString("stop_monitoring") else viewModel.getString("start_monitoring"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = { viewModel.scanNetworks() },
                modifier = Modifier.weight(1f).testTag("scan_now_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = !isScanning
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = VioletAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isScanning) viewModel.getString("scanning") else viewModel.getString("scan_now"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            }
        }

        // Metric Counters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatMetricCard(
                title = viewModel.getString("signal_strength"),
                value = if (currentWifi.isConnected) "${currentWifi.rssi}" else "--",
                unit = "dBm",
                icon = Icons.Default.CellTower,
                iconTint = CyanAccent,
                modifier = Modifier.weight(1f)
            )

            StatMetricCard(
                title = viewModel.getString("link_speed"),
                value = if (currentWifi.isConnected) "${currentWifi.linkSpeedMbps}" else "--",
                unit = "Mbps",
                icon = Icons.Default.Speed,
                iconTint = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatMetricCard(
                title = viewModel.getString("frequency"),
                value = if (currentWifi.isConnected) "${currentWifi.frequencyMhz}" else "--",
                unit = "MHz",
                icon = Icons.Default.Wifi,
                iconTint = VioletAccent,
                modifier = Modifier.weight(1f)
            )

            StatMetricCard(
                title = viewModel.getString("channel"),
                value = channel,
                unit = band,
                icon = Icons.Default.Security,
                iconTint = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        // Real-time Trend Sparkline Card
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "trend_chart_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Signal Trend (${signalTrend.size} samples)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${viewModel.getString("last_updated")}: $lastUpdateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SparklineSignalChart(
                    dataPoints = signalTrend,
                    lineColor = CyanAccent
                )
            }
        }

        // Primary Device Target Badge (OPPO Reno13 F 4G)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = viewModel.getString("oppo_device"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Active Hardware: ${hardwareProfile.manufacturer} ${hardwareProfile.model} • Android ${hardwareProfile.androidVersion} (API ${hardwareProfile.sdkInt})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

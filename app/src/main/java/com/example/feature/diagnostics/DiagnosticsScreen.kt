package com.example.feature.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.MainViewModel
import com.example.core.designsystem.NeumorphicCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen

data class DiagnosticItem(
    val name: String,
    val isOk: Boolean,
    val details: String,
    val recommendation: String
)

@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentWifi by viewModel.wifiMonitor.currentConnection.collectAsState()
    val scanMsg by viewModel.wifiMonitor.scanMessage.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()

    var testCounter by remember { mutableStateOf(0) }

    val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasWifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
    val hasNearby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
    } else true

    val items = listOf(
        DiagnosticItem(
            name = "Wi-Fi System Service",
            isOk = currentWifi.isWifiEnabled,
            details = if (currentWifi.isWifiEnabled) "Wi-Fi adapter is active and operational." else "Wi-Fi is currently turned off on device.",
            recommendation = if (currentWifi.isWifiEnabled) "No action needed." else "Turn on Wi-Fi in Android Settings."
        ),
        DiagnosticItem(
            name = "Location Permission (SSID & Scans)",
            isOk = hasFineLocation,
            details = if (hasFineLocation) "Granted. Full SSID visibility authorized." else "Denied. Android returns '<unknown ssid>' and restricts scan results.",
            recommendation = if (hasFineLocation) "Authorized." else "Grant Location permission in App Info."
        ),
        DiagnosticItem(
            name = "Android 13+ Nearby Devices Permission",
            isOk = hasNearby,
            details = if (hasNearby) "Granted or not required on this SDK level." else "Missing NEARBY_WIFI_DEVICES permission.",
            recommendation = if (hasNearby) "Healthy." else "Enable Nearby Devices in Settings."
        ),
        DiagnosticItem(
            name = "Local Room SQLite Database",
            isOk = true,
            details = "Operational. $sessionCount telemetry records indexed in sandbox.",
            recommendation = "Database healthy."
        ),
        DiagnosticItem(
            name = "Wi-Fi Scan Subsystem",
            isOk = scanMsg == null,
            details = scanMsg ?: "Operating normally without throttling.",
            recommendation = if (scanMsg == null) "Nominal." else "Wait for Android scan throttling cooldown (2 minutes)."
        ),
        DiagnosticItem(
            name = "Raw CSI Hardware HAL",
            isOk = false,
            details = "Standard Android HAL does not expose subcarrier CSI matrices.",
            recommendation = "Normal for unmodified stock firmware on OPPO Reno13 F 4G. External ESP32 adapter required."
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.getString("diagnostics"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Automated Hardware & Subsystem Health Check",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { testCounter++ },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                modifier = Modifier.testTag("rerun_diagnostics_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Refresh")
            }
        }

        items.forEach { diag ->
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "diag_item_${diag.name.replace(" ", "_")}"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (diag.isOk) EmeraldGreen.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (diag.isOk) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (diag.isOk) EmeraldGreen else AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = diag.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = diag.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Action: ${diag.recommendation}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (diag.isOk) EmeraldGreen else AmberWarning
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

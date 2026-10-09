package com.example.feature.experimentalsensing

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.common.EvidenceQuality
import com.example.core.common.SensingStatus
import com.example.core.designsystem.NeumorphicCard
import com.example.core.designsystem.SparklineSignalChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletAccent

@Composable
fun ExperimentalSensingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sensingState by viewModel.sensingState.collectAsState()
    val signalTrend by viewModel.signalTrend.collectAsState()
    val isMonitoring by viewModel.wifiMonitor.isMonitoring.collectAsState()

    var sensitivitySlider by remember { mutableFloatStateOf(1.5f) }
    var windowSlider by remember { mutableIntStateOf(15) }

    val statusColor = when (sensingState.status) {
        SensingStatus.SIGNAL_STABLE -> EmeraldGreen
        SensingStatus.SIGNAL_VARIATION_OBSERVED -> AmberWarning
        SensingStatus.INSUFFICIENT_DATA -> Color(0xFF64748B)
        SensingStatus.INCONCLUSIVE_RESULT -> Color(0xFFEF4444)
    }

    val statusLabel = when (sensingState.status) {
        SensingStatus.SIGNAL_STABLE -> viewModel.getString("status_stable")
        SensingStatus.SIGNAL_VARIATION_OBSERVED -> viewModel.getString("status_variation")
        SensingStatus.INSUFFICIENT_DATA -> viewModel.getString("status_insufficient")
        SensingStatus.INCONCLUSIVE_RESULT -> viewModel.getString("status_inconclusive")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prominent Ethical & Scientific Limitation Banner
        Card(
            modifier = Modifier.fillMaxWidth().testTag("ethical_warning_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0x28F59E0B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Limitation Warning",
                    tint = AmberWarning,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = viewModel.getString("experimental_warning"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFDE68A),
                    lineHeight = 18.sp
                )
            }
        }

        // Sensing Status Dashboard Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "sensing_status_card") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Experimental RF Variation Indicator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = sensingState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Fluctuation Gauge Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fluctuation Index",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f / 10.0", sensingState.fluctuationIndex),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (sensingState.fluctuationIndex / 10f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Metrics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Baseline Mean", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f dBm", sensingState.baselineRssiMean),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text("Window Mean", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f dBm", sensingState.currentRssiMean),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text("Delta |Δ|", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f dB", sensingState.deviationFromBaseline),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Column {
                        Text("Evidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = sensingState.evidenceQuality.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (sensingState.evidenceQuality) {
                                EvidenceQuality.HIGH -> EmeraldGreen
                                EvidenceQuality.MODERATE -> AmberWarning
                                EvidenceQuality.LOW -> Color(0xFFEF4444)
                            }
                        )
                    }
                }
            }
        }

        // Signal Fluctuation Waveform
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "sensing_chart_card") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Live RF Variance Waveform",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                SparklineSignalChart(
                    dataPoints = signalTrend,
                    lineColor = statusColor
                )
            }
        }

        // Configuration Controls
        NeumorphicCard(modifier = Modifier.fillMaxWidth(), testTag = "sensing_config_card") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Experiment Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Sensitivity slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sensitivity Multiplier", style = MaterialTheme.typography.bodySmall)
                        Text("${String.format(java.util.Locale.US, "%.1f", sensitivitySlider)}x", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = sensitivitySlider,
                        onValueChange = {
                            sensitivitySlider = it
                            viewModel.sensingEngine.updateConfig(windowSlider, it.toDouble())
                        },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )
                }

                // Window size slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Observation Window (Samples)", style = MaterialTheme.typography.bodySmall)
                        Text("$windowSlider samples", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = windowSlider.toFloat(),
                        onValueChange = {
                            windowSlider = it.toInt()
                            viewModel.sensingEngine.updateConfig(it.toInt(), sensitivitySlider.toDouble())
                        },
                        valueRange = 5f..40f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = VioletAccent, activeTrackColor = VioletAccent)
                    )
                }

                // Reset and Calibrate actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.resetSensingBaseline() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).testTag("calibrate_baseline_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.getString("calibrate"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

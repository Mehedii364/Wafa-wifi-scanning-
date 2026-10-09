package com.example.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.SignalQualityLevel
import com.example.core.common.WifiUtils
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VioletAccent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shapeRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "neumorphic_card",
    content: @Composable () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    Card(
        modifier = modifier
            .testTag(testTag)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        borderColor
                    )
                ),
                shape = RoundedCornerShape(shapeRadius)
            ),
        shape = RoundedCornerShape(shapeRadius),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    iconTint: Color = CyanAccent,
    modifier: Modifier = Modifier,
    testTag: String = "stat_card"
) {
    NeumorphicCard(
        modifier = modifier,
        testTag = testTag
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SignalQualityBadge(
    rssi: Int,
    modifier: Modifier = Modifier
) {
    val classification = WifiUtils.classifyRssi(rssi)
    val color = Color(WifiUtils.getRssiColorHex(rssi))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "${classification.level.name} (${rssi} dBm)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun SparklineSignalChart(
    dataPoints: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = CyanAccent,
    fillAlpha: Float = 0.2f
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Awaiting signal measurements...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minVal = -95
    val maxVal = -30
    val range = (maxVal - minVal).toFloat()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid guide lines (-50, -70, -90 dBm)
        val guideLevels = listOf(-50, -70, -90)
        guideLevels.forEach { dbm ->
            val normY = 1f - ((dbm - minVal) / range)
            val y = normY * height
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        if (dataPoints.size == 1) {
            val normY = 1f - ((dataPoints[0].coerceIn(minVal, maxVal) - minVal) / range)
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = Offset(width / 2f, normY * height)
            )
            return@Canvas
        }

        val stepX = width / (dataPoints.size - 1)
        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val clamped = value.coerceIn(minVal, maxVal)
            val normY = 1f - ((clamped - minVal) / range)
            val x = index * stepX
            val y = normY * height

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo((dataPoints.size - 1) * stepX, height)
        fillPath.close()

        // Gradient under-fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = fillAlpha), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Main line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw latest data point pulse dot
        val lastIndex = dataPoints.size - 1
        val lastClamped = dataPoints.last().coerceIn(minVal, maxVal)
        val lastNormY = 1f - ((lastClamped - minVal) / range)
        val lastX = lastIndex * stepX
        val lastY = lastNormY * height

        drawCircle(
            color = lineColor.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

@Composable
fun AnimatedRadarScanner(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Outer rings
        drawCircle(
            color = CyanAccent.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = VioletAccent.copy(alpha = 0.2f),
            radius = radius * 0.66f,
            center = center,
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = CyanAccent.copy(alpha = 0.25f),
            radius = radius * 0.33f,
            center = center,
            style = Stroke(width = 1.5f)
        )

        // Crosshairs
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1f
        )

        if (isScanning) {
            val rad = Math.toRadians(angle.toDouble())
            val endX = center.x + (radius * cos(rad)).toFloat()
            val endY = center.y + (radius * sin(rad)).toFloat()

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(CyanAccent, Color.Transparent),
                    start = center,
                    end = Offset(endX, endY)
                ),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center pulse dot
        drawCircle(
            color = if (isScanning) CyanAccent else VioletAccent,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

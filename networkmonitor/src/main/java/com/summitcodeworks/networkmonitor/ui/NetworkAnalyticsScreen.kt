package com.summitcodeworks.networkmonitor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.summitcodeworks.networkmonitor.model.NetworkLog
import kotlin.math.*
import com.summitcodeworks.networkmonitor.ui.theme.*

@Composable
fun NetworkAnalyticsScreen(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Network Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Response Time Chart
        item {
            ResponseTimeChart(logs = logs)
        }

        // Status Code Distribution
        item {
            StatusCodeDistribution(logs = logs)
        }

        // Method Distribution
        item {
            MethodDistribution(logs = logs)
        }

        // Data Transfer Chart
        item {
            DataTransferChart(logs = logs)
        }

        // Top Endpoints
        item {
            TopEndpoints(logs = logs)
        }
    }
}

@Composable
private fun ResponseTimeChart(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Response Time Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val responseTimes = logs.mapNotNull { it.duration }.takeLast(20)
            if (responseTimes.isNotEmpty()) {
                ResponseTimeLineChart(
                    data = responseTimes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = "No response time data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResponseTimeLineChart(
    data: List<Long>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1L
    val minValue = data.minOrNull() ?: 0L
    val range = maxValue - minValue

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val stepX = (width - 2 * padding) / (data.size - 1).coerceAtLeast(1)
        val scaleY = (height - 2 * padding) / range.coerceAtLeast(1)

        // Draw grid lines
        drawGridLines(width, height, padding)

        // Draw line chart
        val points = data.mapIndexed { index, value ->
            Offset(
                x = padding + index * stepX,
                y = height - padding - (value - minValue) * scaleY
            )
        }

        // Draw line
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Accent,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = Accent,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun StatusCodeDistribution(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status Code Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val statusCounts = logs.groupBy { 
                when (it.responseCode) {
                    in 200..299 -> "2xx"
                    in 300..399 -> "3xx"
                    in 400..499 -> "4xx"
                    in 500..599 -> "5xx"
                    else -> "Other"
                }
            }.mapValues { it.value.size }

            StatusCodePieChart(
                data = statusCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun StatusCodePieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0) {
        Text(
            text = "No status code data available",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val colors = ChartColors
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 - 20f
        var startAngle = 0f

        data.entries.forEachIndexed { index, (label, count) ->
            val sweepAngle = (count.toFloat() / total) * 360f
            val color = colors[index % colors.size]

            // Draw pie slice
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun MethodDistribution(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "HTTP Method Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val methodCounts = logs.groupBy { it.method ?: "Unknown" }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }

            methodCounts.forEach { (method, count) ->
                MethodBar(
                    method = method,
                    count = count,
                    maxCount = methodCounts.maxOfOrNull { it.second } ?: 1,
                    color = getMethodColor(method)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MethodBar(
    method: String,
    count: Int,
    maxCount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = method,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        val barWidth = (count.toFloat() / maxCount) * 200f
        
        Box(
            modifier = Modifier
                .width(barWidth.dp)
                .height(20.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DataTransferChart(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Data Transfer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val totalRequestSize = logs.sumOf { it.requestSize }
            val totalResponseSize = logs.sumOf { it.responseSize }
            val totalSize = totalRequestSize + totalResponseSize

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataTransferItem(
                    label = "Request",
                    size = totalRequestSize,
                    totalSize = totalSize,
                    color = Accent
                )
                DataTransferItem(
                    label = "Response",
                    size = totalResponseSize,
                    totalSize = totalSize,
                    color = CustomGreen
                )
            }
        }
    }
}

@Composable
private fun DataTransferItem(
    label: String,
    size: Long,
    totalSize: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatBytes(size),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val percentage = if (totalSize > 0) (size.toFloat() / totalSize * 100) else 0f
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopEndpoints(
    logs: List<NetworkLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top Endpoints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val endpointCounts = logs.groupBy { it.url }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)

            endpointCounts.forEach { (url, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = url,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = count.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun DrawScope.drawGridLines(width: Float, height: Float, padding: Float) {
    val gridColor = Divider.copy(alpha = 0.3f)
    
    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (height - 2 * padding) * i / 4
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Vertical grid lines
    for (i in 0..4) {
        val x = padding + (width - 2 * padding) * i / 4
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> GetMethodColor
        "POST" -> PostMethodColor
        "PUT" -> PutMethodColor
        "DELETE" -> DeleteMethodColor
        "PATCH" -> PatchMethodColor
        else -> Color.Gray
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return String.format("%.1f %s", size, units[unitIndex])
}

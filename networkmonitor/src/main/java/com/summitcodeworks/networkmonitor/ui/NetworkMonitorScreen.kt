package com.summitcodeworks.networkmonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import com.summitcodeworks.networkmonitor.model.WebSocketEventType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkMonitorScreen(
    onNavigateToDetails: (Long) -> Unit = {},
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val networkSummary by viewModel.networkSummary.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Network Monitor") },
            actions = {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear logs")
                }
                IconButton(onClick = { /* TODO: Export */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Export logs")
                }
            }
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        // Tabs
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            NetworkMonitorTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.name) }
                )
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                NetworkMonitorTab.HTTP -> HttpLogsContent(viewModel, onNavigateToDetails)
                NetworkMonitorTab.WEBSOCKET -> WebSocketEventsContent(viewModel)
                NetworkMonitorTab.SUMMARY -> SummaryContent(networkSummary)
                NetworkMonitorTab.FAILED -> FailedRequestsContent(viewModel, onNavigateToDetails)
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs") },
            text = { Text("Are you sure you want to clear all network logs? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HttpLogsContent(
    viewModel: NetworkMonitorViewModel,
    onNavigateToDetails: (Long) -> Unit
) {
    val logs by viewModel.networkLogs.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(logs) { log ->
            NetworkLogItem(
                log = log,
                onClick = { onNavigateToDetails(log.id) }
            )
        }
    }
}

@Composable
private fun WebSocketEventsContent(viewModel: NetworkMonitorViewModel) {
    val events by viewModel.webSocketEvents.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(events) { event ->
            WebSocketEventItem(event = event)
        }
    }
}

@Composable
private fun SummaryContent(summary: com.summitcodeworks.networkmonitor.model.NetworkSummary?) {
    if (summary == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No data available")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SummaryCard(
                title = "Total Requests",
                value = summary.totalRequests.toString(),
                icon = Icons.Default.Info
            )
        }
        item {
            SummaryCard(
                title = "Successful",
                value = summary.successfulRequests.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color.Green
            )
        }
        item {
            SummaryCard(
                title = "Failed",
                value = summary.failedRequests.toString(),
                icon = Icons.Default.Error,
                color = Color.Red
            )
        }
        item {
            SummaryCard(
                title = "Data Transferred",
                value = formatBytes(summary.totalDataTransferred),
                icon = Icons.Default.CloudUpload
            )
        }
        item {
            SummaryCard(
                title = "Avg Response Time",
                value = "${summary.averageResponseTime}ms",
                icon = Icons.Default.Timer
            )
        }
    }
}

@Composable
private fun FailedRequestsContent(
    viewModel: NetworkMonitorViewModel,
    onNavigateToDetails: (Long) -> Unit
) {
    val failedRequests by viewModel.failedRequests.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(failedRequests) { log ->
            NetworkLogItem(
                log = log,
                onClick = { onNavigateToDetails(log.id) }
            )
        }
    }
}

@Composable
private fun NetworkLogItem(
    log: NetworkLog,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.method ?: "UNKNOWN",
                    fontWeight = FontWeight.Bold,
                    color = getMethodColor(log.method)
                )
                StatusChip(log.responseCode)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.url,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(log.requestTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.duration != null) {
                    Text(
                        text = "${log.duration}ms",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WebSocketEventItem(event: WebSocketEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.eventType.name,
                    fontWeight = FontWeight.Bold,
                    color = getEventTypeColor(event.eventType)
                )
                Text(
                    text = formatTimestamp(event.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.url,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (event.message != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.message,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (event.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: ${event.error}",
                    fontSize = 12.sp,
                    color = Color.Red,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusChip(responseCode: Int?) {
    val color = when (responseCode) {
        in 200..299 -> Color.Green
        in 300..399 -> Color.Blue
        in 400..499 -> Color(0xFFFF9800)
        in 500..599 -> Color.Red
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = responseCode?.toString() ?: "---",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun getMethodColor(method: String?): Color {
    return when (method?.uppercase()) {
        "GET" -> Color.Blue
        "POST" -> Color.Green
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color.Red
        "PATCH" -> Color.Magenta
        else -> Color.Gray
    }
}

private fun getEventTypeColor(eventType: WebSocketEventType): Color {
    return when (eventType) {
        WebSocketEventType.OPEN -> Color.Green
        WebSocketEventType.CLOSED -> Color.Blue
        WebSocketEventType.MESSAGE_SENT, WebSocketEventType.MESSAGE_RECEIVED -> Color.Cyan
        WebSocketEventType.FAILURE -> Color.Red
        else -> Color.Gray
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
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
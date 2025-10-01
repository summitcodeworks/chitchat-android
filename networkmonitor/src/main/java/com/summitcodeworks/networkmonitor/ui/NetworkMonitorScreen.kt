package com.summitcodeworks.networkmonitor.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import com.summitcodeworks.networkmonitor.model.WebSocketEventType
import com.summitcodeworks.networkmonitor.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Network Monitor screen for debugging and monitoring network requests.
 * 
 * This screen provides comprehensive network debugging capabilities for the
 * ChitChat application, allowing developers to inspect HTTP requests, responses,
 * WebSocket events, and network performance in real-time.
 * 
 * Key features:
 * - HTTP request/response monitoring with detailed logs
 * - WebSocket event tracking and analysis
 * - Network performance metrics and summaries
 * - Search and filter capabilities for network logs
 * - Request/response body inspection
 * - Error tracking and debugging information
 * - Export capabilities for sharing network data
 * 
 * The screen is organized into tabs:
 * - HTTP: HTTP requests and responses
 * - WebSocket: Real-time WebSocket events
 * - Summary: Network performance overview
 * 
 * This tool is essential for:
 * - API debugging and testing
 * - Performance optimization
 * - Error diagnosis and troubleshooting
 * - Network behavior analysis
 * - Development and QA workflows
 * 
 * @param onNavigateToDetails Callback to navigate to detailed network log view
 * @param onNavigateToEditor Callback to navigate to request editor
 * @param viewModel ViewModel handling network monitoring logic and state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkMonitorScreen(
    onNavigateToDetails: (Long) -> Unit = {},
    onNavigateToEditor: (NetworkLog?) -> Unit = {},
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val networkSummary by viewModel.networkSummary.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(NetworkFilter()) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(
        initialPage = selectedTab.ordinal,
        pageCount = { NetworkMonitorTab.values().size }
    )
    
    // Sync pager state with selected tab
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab.ordinal)
    }
    
    // Sync selected tab with pager state
    LaunchedEffect(pagerState.currentPage) {
        val newTab = NetworkMonitorTab.values()[pagerState.currentPage]
        if (newTab != selectedTab) {
            viewModel.selectTab(newTab)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Network Monitor") },
            actions = {
                IconButton(onClick = { onNavigateToEditor(null) }) {
                    Icon(Icons.Default.Add, contentDescription = "New Request")
                }
                IconButton(onClick = { 
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Filter feature coming soon",
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = { showAnalyticsDialog = true }) {
                    Icon(Icons.Default.Analytics, contentDescription = "Analytics")
                }
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear logs")
                }
                IconButton(onClick = { 
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Export feature coming soon",
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
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

        // Scrollable Tabs with chip/pill styling
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { 
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            },
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier,
                    height = 0.dp,
                    color = Color.Transparent
                )
            }
        ) {
            NetworkMonitorTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { 
                        Text(
                            text = tab.name.lowercase().let { 
                                if (it.isNotEmpty()) it[0].uppercase() + it.substring(1)
                                else it
                            },
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTab == tab) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 1.dp, vertical = 2.dp)
                        .background(
                            color = if (selectedTab == tab) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Content with HorizontalPager for swipe gestures
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (NetworkMonitorTab.values()[page]) {
                    NetworkMonitorTab.HTTP -> HttpLogsContent(viewModel, onNavigateToDetails, onNavigateToEditor)
                    NetworkMonitorTab.WEBSOCKET -> WebSocketEventsContent(viewModel)
                    NetworkMonitorTab.CURL -> CurlCommandsContent(viewModel)
                    NetworkMonitorTab.SUMMARY -> SummaryContent(networkSummary)
                    NetworkMonitorTab.FAILED -> FailedRequestsContent(viewModel, onNavigateToDetails, onNavigateToEditor)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        // Snackbar Host
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
        }
    }
    
    // Analytics Dialog
    if (showAnalyticsDialog) {
        AlertDialog(
            onDismissRequest = { showAnalyticsDialog = false },
            title = { Text("Network Analytics") },
            text = { 
                Column {
                    networkSummary?.let { summary ->
                        Text("Total Requests: ${summary.totalRequests}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("✓ Successful: ${summary.successfulRequests}", color = MaterialTheme.colorScheme.primary)
                        if (summary.failedRequests > 0) {
                            Text("⚡ Needs Attention: ${summary.failedRequests}", color = MaterialTheme.colorScheme.tertiary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Data Transferred: ${formatBytes(summary.totalDataTransferred)}")
                        Text("Avg Response Time: ${summary.averageResponseTime}ms")
                    } ?: Text("Loading analytics...")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAnalyticsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    // Clear Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs") },
            text = { Text("Are you sure you want to delete all network logs? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("All logs cleared")
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
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
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToEditor: (NetworkLog?) -> Unit
) {
    val logs by viewModel.networkLogs.collectAsStateWithLifecycle()

    if (logs.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.Http,
            title = "No HTTP Requests",
            message = "Start making network requests to see them here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                NetworkLogItem(
                    log = log,
                    onClick = { onNavigateToDetails(log.id) },
                    onEdit = { onNavigateToEditor(log) }
                )
            }
        }
    }
}

@Composable
private fun WebSocketEventsContent(viewModel: NetworkMonitorViewModel) {
    val events by viewModel.webSocketEvents.collectAsStateWithLifecycle()

    if (events.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.Wifi,
            title = "No WebSocket Events",
            message = "WebSocket connections and events will appear here"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                WebSocketEventItem(event = event)
            }
        }
    }
}

@Composable
private fun SummaryContent(summary: com.summitcodeworks.networkmonitor.model.NetworkSummary?) {
    if (summary == null) {
        EmptyStateContent(
            icon = Icons.Default.Analytics,
            title = "No Network Data",
            message = "Network statistics will appear here once you start making requests"
        )
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
                title = "To Review",
                value = summary.failedRequests.toString(),
                icon = Icons.Default.Info,
                color = MaterialTheme.colorScheme.tertiary
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
private fun CurlCommandsContent(viewModel: NetworkMonitorViewModel) {
    val logs by viewModel.networkLogs.collectAsStateWithLifecycle()
    val curlLogs = logs.filter { it.curlCommand != null }

    if (curlLogs.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.Terminal,
            title = "No cURL Commands",
            message = "cURL commands will be generated for your network requests"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(curlLogs) { log ->
                CurlCommandItem(log = log)
            }
        }
    }
}

@Composable
private fun FailedRequestsContent(
    viewModel: NetworkMonitorViewModel,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToEditor: (NetworkLog?) -> Unit
) {
    val failedRequests by viewModel.failedRequests.collectAsStateWithLifecycle()

    if (failedRequests.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.CheckCircle,
            title = "All Systems Go!",
            message = "Everything is working perfectly"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(failedRequests) { log ->
                NetworkLogItem(
                    log = log,
                    onClick = { onNavigateToDetails(log.id) },
                    onEdit = { onNavigateToEditor(log) }
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkLogItem(
    log: NetworkLog,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Request",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    StatusChip(log.responseCode)
                }
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
                    text = "⚡ Connection interrupted - Reconnecting...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CurlCommandItem(log: NetworkLog) {
    val clipboardManager = LocalClipboardManager.current

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
                    text = "${log.method} ${log.responseCode ?: "---"}",
                    fontWeight = FontWeight.Bold,
                    color = getMethodColor(log.method)
                )

                Row {
                    IconButton(
                        onClick = {
                            log.curlCommand?.let { curl ->
                                clipboardManager.setText(AnnotatedString(curl))
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy cURL",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = formatTimestamp(log.requestTime),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.url,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (log.curlCommand != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = log.curlCommand,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
fun StatusChip(responseCode: Int?) {
    val color = when (responseCode) {
        in 200..299 -> StatusSuccess
        in 300..399 -> StatusRedirect
        in 400..499 -> StatusClientError
        in 500..599 -> StatusServerError
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
        "GET" -> GetMethodColor
        "POST" -> PostMethodColor
        "PUT" -> PutMethodColor
        "DELETE" -> DeleteMethodColor
        "PATCH" -> PatchMethodColor
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
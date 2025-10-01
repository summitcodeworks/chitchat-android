package com.summitcodeworks.networkmonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*
import com.summitcodeworks.networkmonitor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NetworkLogDetailsScreen(
    logId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (NetworkLog?) -> Unit = {},
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val log by viewModel.getLogById(logId).collectAsStateWithLifecycle(initialValue = null)
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 4 })
    var showCopyDialog by remember { mutableStateOf(false) }
    var copyContent by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf(0) }
    
    // Auto-refresh effect to catch late-arriving response data (max 3 retries)
    LaunchedEffect(log?.responseBody, log?.responseCode) {
        if (log != null && log?.responseBody == null && log?.responseCode != null && refreshKey < 3) {
            // If we have a response code but no body, wait a bit and refresh
            kotlinx.coroutines.delay(300)
            refreshKey++
        }
    }

    if (log == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentLog = log!! // Create a local non-null reference

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "${currentLog.method} ${currentLog.responseCode ?: "---"}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { 
                    onNavigateToEditor(currentLog)
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit & Replay")
                }
                IconButton(onClick = { 
                    copyContent = currentLog.curlCommand ?: ""
                    showCopyDialog = true
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy cURL")
                }
                IconButton(onClick = { 
                    copyContent = formatLogForSharing(currentLog)
                    showCopyDialog = true
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        )

        // URL and Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = currentLog.method ?: "UNKNOWN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = getMethodColor(currentLog.method)
                    )
                    StatusChip(currentLog.responseCode)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentLog.url,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        copyContent = currentLog.url
                        showCopyDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duration: ${currentLog.duration ?: 0}ms",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Size: ${formatBytes(currentLog.requestSize + currentLog.responseSize)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentLog.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Request couldn't be completed. Tap to retry or check your connection.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Tabs
        TabRow(selectedTabIndex = pagerState.currentPage) {
            listOf("Request", "Response", "Headers", "cURL").forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        // Swipeable Tab Content with HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> RequestContent(currentLog, clipboardManager) { content ->
                    copyContent = content
                    showCopyDialog = true
                }
                1 -> ResponseContent(currentLog, clipboardManager) { content ->
                    copyContent = content
                    showCopyDialog = true
                }
                2 -> HeadersContent(currentLog, clipboardManager) { content ->
                    copyContent = content
                    showCopyDialog = true
                }
                3 -> CurlContent(currentLog, clipboardManager) { content ->
                    copyContent = content
                    showCopyDialog = true
                }
            }
        }
    }

    // Copy dialog
    if (showCopyDialog) {
        // Copy to clipboard immediately when dialog shows
        LaunchedEffect(copyContent) {
            clipboardManager.setText(AnnotatedString(copyContent))
        }
        
        AlertDialog(
            onDismissRequest = { showCopyDialog = false },
            title = { Text("Copied!") },
            text = { 
                Text("Content has been copied to clipboard")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCopyDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun RequestContent(
    log: NetworkLog,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Request Headers
        if (!log.requestHeaders.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Request Headers",
                    content = log.requestHeaders,
                    onCopy = onCopy
                )
            }
        }

        // Request Body
        if (!log.requestBody.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Request Body",
                    content = formatJsonIfPossible(log.requestBody),
                    onCopy = onCopy,
                    isJson = isJsonContent(log.requestBody)
                )
            }
        } else {
            item {
                Text(
                    text = "No request body",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResponseContent(
    log: NetworkLog,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Response Headers
        if (!log.responseHeaders.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Response Headers",
                    content = log.responseHeaders,
                    onCopy = onCopy
                )
            }
        }

        // Response Body
        if (!log.responseBody.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Response Body",
                    content = formatJsonIfPossible(log.responseBody),
                    onCopy = onCopy,
                    isJson = isJsonContent(log.responseBody)
                )
            }
        } else {
            item {
                Text(
                    text = "No response body",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HeadersContent(
    log: NetworkLog,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!log.requestHeaders.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Request Headers",
                    content = log.requestHeaders,
                    onCopy = onCopy
                )
            }
        }

        if (!log.responseHeaders.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "Response Headers",
                    content = log.responseHeaders,
                    onCopy = onCopy
                )
            }
        }
    }
}

@Composable
private fun CurlContent(
    log: NetworkLog,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onCopy: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!log.curlCommand.isNullOrEmpty()) {
            item {
                ExpandableSection(
                    title = "cURL Command",
                    content = log.curlCommand,
                    onCopy = onCopy,
                    isMonospace = true
                )
            }
        } else {
            item {
                Text(
                    text = "No cURL command available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    content: String,
    onCopy: (String) -> Unit,
    isJson: Boolean = false,
    isMonospace: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row {
                    IconButton(
                        onClick = { onCopy(content) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            if (isExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp), // Limit max height for scrollability
                    color = if (isJson) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = content,
                            modifier = Modifier.padding(16.dp),
                            fontSize = if (isMonospace) 12.sp else 14.sp,
                            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatJsonIfPossible(content: String): String {
    return try {
        val jsonParser = JsonParser()
        val jsonElement = jsonParser.parse(content)
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        gson.toJson(jsonElement)
    } catch (e: Exception) {
        content
    }
}

private fun isJsonContent(content: String): Boolean {
    return try {
        val jsonParser = JsonParser()
        jsonParser.parse(content)
        true
    } catch (e: Exception) {
        false
    }
}

private fun formatLogForSharing(log: NetworkLog): String {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(log.requestTime))
    
    return buildString {
        appendLine("Network Request Details")
        appendLine("=====================")
        appendLine("Time: $timestamp")
        appendLine("Method: ${log.method}")
        appendLine("URL: ${log.url}")
        appendLine("Status: ${log.responseCode ?: "Unknown"}")
        appendLine("Duration: ${log.duration ?: 0}ms")
        appendLine("Size: ${formatBytes(log.requestSize + log.responseSize)}")
        if (log.error != null) {
            appendLine("Error: ${log.error}")
        }
        if (!log.curlCommand.isNullOrEmpty()) {
            appendLine("\ncURL Command:")
            appendLine(log.curlCommand)
        }
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

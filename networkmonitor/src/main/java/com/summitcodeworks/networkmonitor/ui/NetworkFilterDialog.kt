package com.summitcodeworks.networkmonitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.summitcodeworks.networkmonitor.ui.theme.*

data class NetworkFilter(
    val statusCodes: Set<Int> = emptySet(),
    val methods: Set<String> = emptySet(),
    val timeRange: Pair<Long, Long>? = null,
    val showOnlyErrors: Boolean = false,
    val minDuration: Long? = null,
    val maxDuration: Long? = null
)

@Composable
fun NetworkFilterDialog(
    currentFilter: NetworkFilter,
    onFilterApplied: (NetworkFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var statusCodes by remember { mutableStateOf(currentFilter.statusCodes) }
    var methods by remember { mutableStateOf(currentFilter.methods) }
    var showOnlyErrors by remember { mutableStateOf(currentFilter.showOnlyErrors) }
    var minDuration by remember { mutableStateOf(currentFilter.minDuration?.toString() ?: "") }
    var maxDuration by remember { mutableStateOf(currentFilter.maxDuration?.toString() ?: "") }
    var selectedTimeRange by remember { mutableStateOf(0) }

    val timeRanges = listOf(
        "All Time" to null,
        "Last Hour" to 60 * 60 * 1000L,
        "Last 6 Hours" to 6 * 60 * 60 * 1000L,
        "Last 24 Hours" to 24 * 60 * 60 * 1000L,
        "Last Week" to 7 * 24 * 60 * 60 * 1000L
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Network Logs", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Codes
                item {
                    Text("Status Codes", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    StatusCodeFilter(
                        selectedCodes = statusCodes,
                        onSelectionChanged = { statusCodes = it }
                    )
                }

                // HTTP Methods
                item {
                    Text("HTTP Methods", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    MethodFilter(
                        selectedMethods = methods,
                        onSelectionChanged = { methods = it }
                    )
                }

                // Time Range
                item {
                    Text("Time Range", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    TimeRangeFilter(
                        selectedIndex = selectedTimeRange,
                        onSelectionChanged = { selectedTimeRange = it },
                        timeRanges = timeRanges
                    )
                }

                // Duration Filter
                item {
                    Text("Duration (ms)", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minDuration,
                            onValueChange = { minDuration = it },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maxDuration,
                            onValueChange = { maxDuration = it },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Error Filter
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = showOnlyErrors,
                            onCheckedChange = { showOnlyErrors = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show only errors")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val timeRange = timeRanges[selectedTimeRange].second?.let { duration ->
                        val endTime = System.currentTimeMillis()
                        val startTime = endTime - duration
                        startTime to endTime
                    }

                    val filter = NetworkFilter(
                        statusCodes = statusCodes,
                        methods = methods,
                        timeRange = timeRange,
                        showOnlyErrors = showOnlyErrors,
                        minDuration = minDuration.toLongOrNull(),
                        maxDuration = maxDuration.toLongOrNull()
                    )
                    onFilterApplied(filter)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatusCodeFilter(
    selectedCodes: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    val statusCodeRanges = listOf(
        "2xx Success" to (200..299),
        "3xx Redirection" to (300..399),
        "4xx Client Error" to (400..499),
        "5xx Server Error" to (500..599)
    )

    statusCodeRanges.forEach { (label, range) ->
        val isSelected = range.any { it in selectedCodes }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                    if (checked) {
                        onSelectionChanged(selectedCodes + range)
                    } else {
                        onSelectionChanged(selectedCodes - range)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
private fun MethodFilter(
    selectedMethods: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

    methods.forEach { method ->
        val isSelected = method in selectedMethods
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                    if (checked) {
                        onSelectionChanged(selectedMethods + method)
                    } else {
                        onSelectionChanged(selectedMethods - method)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = method,
                color = getMethodColor(method)
            )
        }
    }
}

@Composable
private fun TimeRangeFilter(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    timeRanges: List<Pair<String, Long?>>
) {
    timeRanges.forEachIndexed { index, (label, _) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedIndex == index,
                onClick = { onSelectionChanged(index) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}

private fun getMethodColor(method: String): Color {
    return when (method.uppercase()) {
        "GET" -> Color.Blue
        "POST" -> Color.Green
        "PUT" -> PutMethodColor
        "DELETE" -> Color.Red
        "PATCH" -> Color.Magenta
        "HEAD" -> Color.Cyan
        "OPTIONS" -> Color.Gray
        else -> Color.Gray
    }
}

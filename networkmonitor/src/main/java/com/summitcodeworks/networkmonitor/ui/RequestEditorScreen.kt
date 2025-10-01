package com.summitcodeworks.networkmonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.networkmonitor.model.NetworkLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EditableRequest(
    val method: String = "GET",
    val url: String = "",
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    val contentType: String = "application/json"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestEditorScreen(
    originalLog: NetworkLog? = null,
    onNavigateBack: () -> Unit,
    onRequestSent: (NetworkLog) -> Unit,
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    var isSending by remember { mutableStateOf(false) }
    var showHeadersDialog by remember { mutableStateOf(false) }
    var showCurlDialog by remember { mutableStateOf(false) }
    var curlCommand by remember { mutableStateOf("") }
    
    // Initialize editable request from original log
    val editableRequest = remember(originalLog) {
        originalLog?.let { log ->
            EditableRequest(
                method = log.method ?: "GET",
                url = log.url,
                headers = parseHeaders(log.requestHeaders),
                body = log.requestBody ?: "",
                contentType = extractContentType(log.requestHeaders) ?: "application/json"
            )
        } ?: EditableRequest()
    }
    
    var method by remember { mutableStateOf(editableRequest.method) }
    var url by remember { mutableStateOf(editableRequest.url) }
    var headers by remember { mutableStateOf(editableRequest.headers) }
    var body by remember { mutableStateOf(editableRequest.body) }
    var contentType by remember { mutableStateOf(editableRequest.contentType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = if (originalLog != null) "Edit Request" else "New Request",
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
                    curlCommand = generateCurlCommand(method, url, headers, body)
                    showCurlDialog = true
                }) {
                    Icon(Icons.Default.Code, contentDescription = "View cURL")
                }
                IconButton(
                    onClick = { 
                        scope.launch {
                            isSending = true
                            try {
                                viewModel.sendRequest(method, url, headers, body, contentType)
                                onNavigateBack()
                            } catch (e: Exception) {
                                // Handle error
                            } finally {
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending && url.isNotBlank()
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send Request")
                    }
                }
            }
        )

        // Request Form
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Method selector with horizontal pills
            item {
                var expanded by remember { mutableStateOf(false) }
                val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Method",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        methods.take(4).forEach { methodOption ->
                            FilterChip(
                                selected = method == methodOption,
                                onClick = { method = methodOption },
                                label = { 
                                    Text(
                                        text = methodOption,
                                        fontSize = 13.sp,
                                        fontWeight = if (method == methodOption) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when(methodOption) {
                                        "GET" -> Color(0xFF4CAF50)
                                        "POST" -> Color(0xFF2196F3)
                                        "PUT" -> Color(0xFFFF9800)
                                        "DELETE" -> Color(0xFFF44336)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(36.dp)
                            )
                        }
                        
                        // More options
                        Box {
                            FilterChip(
                                selected = method in methods.drop(4),
                                onClick = { expanded = true },
                                label = { 
                                    Text(
                                        text = if (method in methods.drop(4)) method else "•••",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.height(36.dp)
                            )
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                methods.drop(4).forEach { methodOption ->
                                    DropdownMenuItem(
                                        text = { Text(methodOption) },
                                        onClick = {
                                            method = methodOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // URL field
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "URL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        placeholder = { Text("https://api.example.com/endpoint") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Headers
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
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
                                text = "Headers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (headers.isNotEmpty()) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${headers.size}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                IconButton(onClick = { showHeadersDialog = true }) {
                                    Icon(
                                        imageVector = if (headers.isEmpty()) Icons.Default.Add else Icons.Default.Edit,
                                        contentDescription = "Edit Headers",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        if (headers.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "No headers added",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                headers.forEach { (key, value) ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = key,
                                                    fontWeight = FontWeight.Medium,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Body (for POST, PUT, PATCH)
            if (method in listOf("POST", "PUT", "PATCH")) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Request Body",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Content Type
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Content-Type",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                OutlinedTextField(
                                    value = contentType,
                                    onValueChange = { contentType = it },
                                    placeholder = { Text("application/json") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Body content
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Body Content",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                OutlinedTextField(
                                    value = body,
                                    onValueChange = { body = it },
                                    placeholder = { Text("{\n  \"key\": \"value\"\n}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    maxLines = 10,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Headers Editor Dialog
    if (showHeadersDialog) {
        HeadersEditorDialog(
            headers = headers,
            onHeadersChanged = { headers = it },
            onDismiss = { showHeadersDialog = false }
        )
    }

    // cURL Dialog
    if (showCurlDialog) {
        CurlDialog(
            curlCommand = curlCommand,
            onCopy = { 
                clipboardManager.setText(AnnotatedString(curlCommand))
            },
            onDismiss = { showCurlDialog = false }
        )
    }
}

@Composable
private fun HeaderItem(
    key: String,
    value: String,
    onEdit: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editKey by remember { mutableStateOf(key) }
    var editValue by remember { mutableStateOf(value) }

    if (isEditing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = editKey,
                onValueChange = { editKey = it },
                label = { Text("Key") },
                modifier = Modifier.weight(0.4f),
                singleLine = true
            )
            OutlinedTextField(
                value = editValue,
                onValueChange = { editValue = it },
                label = { Text("Value") },
                modifier = Modifier.weight(0.6f),
                singleLine = true
            )
            IconButton(onClick = { 
                onEdit(editKey, editValue)
                isEditing = false
            }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
            IconButton(onClick = { 
                editKey = key
                editValue = value
                isEditing = false
            }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = key,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeadersEditorDialog(
    headers: Map<String, String>,
    onHeadersChanged: (Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    var newHeaders by remember { mutableStateOf(headers.toMutableMap()) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Headers") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add new header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            label = { Text("Key") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                    newHeaders[newKey] = newValue
                                    newKey = ""
                                    newValue = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }

                // Existing headers
                items(newHeaders.toList()) { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = key,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = value,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { newHeaders.remove(key) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onHeadersChanged(newHeaders)
                    onDismiss()
                }
            ) {
                Text("Save")
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
private fun CurlDialog(
    curlCommand: String,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("cURL Command") },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = curlCommand,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCopy) {
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper functions
private fun parseHeaders(headersJson: String?): Map<String, String> {
    if (headersJson.isNullOrBlank()) return emptyMap()
    
    return try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
        val headerMap: Map<String, List<String>> = gson.fromJson(headersJson, type)
        headerMap.mapValues { it.value.firstOrNull() ?: "" }
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun extractContentType(headersJson: String?): String? {
    val headers = parseHeaders(headersJson)
    return headers["Content-Type"] ?: headers["content-type"]
}

private fun generateCurlCommand(
    method: String,
    url: String,
    headers: Map<String, String>,
    body: String
): String {
    val curlBuilder = StringBuilder("curl")

    // Add method
    if (method != "GET") {
        curlBuilder.append(" -X $method")
    }

    // Add headers
    headers.forEach { (key, value) ->
        // Properly escape header values
        val escapedValue = value.replace("\"", "\\\"")
        curlBuilder.append(" \\\n  -H \"$key: $escapedValue\"")
    }

    // Add body for POST/PUT/PATCH requests
    if (body.isNotBlank() && method in listOf("POST", "PUT", "PATCH")) {
        val contentType = (headers["Content-Type"] ?: headers["content-type"])?.lowercase()

        when {
            contentType?.contains("application/json") == true -> {
                // For JSON, use proper escaping and formatting
                val escapedBody = body
                    .replace("\\", "\\\\")  // Escape backslashes first
                    .replace("\"", "\\\"")  // Escape double quotes
                    .replace("\n", "\\n")   // Escape newlines
                    .replace("\r", "\\r")   // Escape carriage returns
                    .replace("\t", "\\t")   // Escape tabs
                curlBuilder.append(" \\\n  -d \"$escapedBody\"")
            }
            contentType?.contains("application/x-www-form-urlencoded") == true -> {
                // For form data, we can use single quotes safely if no single quotes in data
                if (body.contains("'")) {
                    val escapedBody = body.replace("\"", "\\\"")
                    curlBuilder.append(" \\\n  -d \"$escapedBody\"")
                } else {
                    curlBuilder.append(" \\\n  -d '$body'")
                }
            }
            else -> {
                // For other content types, try to escape appropriately
                if (body.contains("\"") && !body.contains("'")) {
                    curlBuilder.append(" \\\n  -d '$body'")
                } else {
                    val escapedBody = body.replace("\"", "\\\"")
                    curlBuilder.append(" \\\n  -d \"$escapedBody\"")
                }
            }
        }
    }

    // Add URL (always last)
    curlBuilder.append(" \\\n  \"$url\"")

    return curlBuilder.toString()
}


package com.summitcodeworks.chitchat.presentation.screen.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.summitcodeworks.networkmonitor.NetworkMonitor
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.EnvironmentViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    environmentViewModel: EnvironmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var logsCount by remember { mutableIntStateOf(0) }

    val currentEnvironment by environmentViewModel.currentEnvironment.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        NetworkMonitor.getInstance()?.let { monitor ->
            logsCount = monitor.getLogsCount()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Debug Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DebugCard(
                    title = "Current Environment",
                    description = "${currentEnvironment.displayName} - ${currentEnvironment.apiBaseUrl}",
                    icon = Icons.Default.Settings,
                    onClick = {
                        // Could add environment switching here if needed
                    }
                )
            }

            item {
                DebugCard(
                    title = "Network Monitor",
                    description = "View HTTP requests and WebSocket events",
                    icon = Icons.Default.NetworkCheck,
                    badge = logsCount.toString(),
                    onClick = {
                        NetworkMonitor.getInstance()?.launchUI()
                    }
                )
            }

            item {
                DebugCard(
                    title = "Clear Network Logs",
                    description = "Clear all network monitoring data",
                    icon = Icons.Default.Delete,
                    onClick = {
                        scope.launch {
                            NetworkMonitor.getInstance()?.clearLogs()
                            logsCount = 0
                        }
                    }
                )
            }

            item {
                DebugCard(
                    title = "App Info",
                    description = "Build config and app information",
                    icon = Icons.Default.Info,
                    onClick = {
                        // TODO: Show app info
                    }
                )
            }

            item {
                DebugCard(
                    title = "Database Inspector",
                    description = "View local database contents",
                    icon = Icons.Default.Storage,
                    onClick = {
                        // TODO: Database inspector
                    }
                )
            }

            item {
                DebugCard(
                    title = "Crash Test",
                    description = "Test crash reporting",
                    icon = Icons.Default.BugReport,
                    onClick = {
                        throw RuntimeException("Test crash from debug menu")
                    }
                )
            }
        }
    }
}

@Composable
private fun DebugCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (badge != null) {
                Badge(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(badge)
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
package com.summitcodeworks.networkmonitor.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.ui.theme.NetworkMonitorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NetworkMonitorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetworkMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetworkMonitorNavigation()
                }
            }
        }
    }
}

@Composable
private fun NetworkMonitorNavigation() {
    var currentScreen by remember { mutableStateOf(NetworkMonitorScreenType.MAIN) }
    var selectedLogId by remember { mutableStateOf<Long?>(null) }
    var selectedLog by remember { mutableStateOf<NetworkLog?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                NetworkMonitorScreenType.MAIN -> {
                    NetworkMonitorScreen(
                        onNavigateToDetails = { logId ->
                            selectedLogId = logId
                            currentScreen = NetworkMonitorScreenType.DETAILS
                        },
                        onNavigateToEditor = { log ->
                            selectedLog = log
                            currentScreen = NetworkMonitorScreenType.EDITOR
                        }
                    )
                }
                
                NetworkMonitorScreenType.DETAILS -> {
                    selectedLogId?.let { logId ->
                        NetworkLogDetailsScreen(
                            logId = logId,
                            onNavigateBack = {
                                currentScreen = NetworkMonitorScreenType.MAIN
                                selectedLogId = null
                            },
                            onNavigateToEditor = { log ->
                                selectedLog = log
                                currentScreen = NetworkMonitorScreenType.EDITOR
                            }
                        )
                    }
                }
                
                NetworkMonitorScreenType.EDITOR -> {
                    RequestEditorScreen(
                        originalLog = selectedLog,
                        onNavigateBack = {
                            currentScreen = if (selectedLogId != null) {
                                NetworkMonitorScreenType.DETAILS
                            } else {
                                NetworkMonitorScreenType.MAIN
                            }
                            selectedLog = null
                        },
                        onRequestSent = { newLog ->
                            // Request was sent, navigate back to main screen
                            currentScreen = NetworkMonitorScreenType.MAIN
                            selectedLog = null
                            selectedLogId = null
                        }
                    )
                }
            }
        }
    }
}

private enum class NetworkMonitorScreenType {
    MAIN, DETAILS, EDITOR
}
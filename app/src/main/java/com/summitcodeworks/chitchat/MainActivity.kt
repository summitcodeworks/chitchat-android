package com.summitcodeworks.chitchat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.summitcodeworks.chitchat.presentation.navigation.ChitChatNavigation
import com.summitcodeworks.chitchat.ui.theme.ChitChatTheme
import com.summitcodeworks.networkmonitor.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, restart Network Monitor to ensure it works properly
            startNetworkMonitor()
        } else {
            // Permission denied, Network Monitor will still work but without notifications
            startNetworkMonitor()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Network Monitor on Android 13+
        requestNotificationPermission()
        
        // Start Network Monitor notification service
        startNetworkMonitor()
        
        // Configure status bar for white background with black text
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }
        window.statusBarColor = android.graphics.Color.WHITE
        
        setContent {
            ChitChatTheme {
                ChitChatNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, ensure Network Monitor is running
                    startNetworkMonitor()
                }
                else -> {
                    // Request the permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, no notification permission needed
            startNetworkMonitor()
        }
    }
    
    private fun startNetworkMonitor() {
        try {
            // Ensure Network Monitor is initialized
            val networkMonitor = NetworkMonitor.getInstance() ?: NetworkMonitor.initialize(this)
            
            // Start Network Monitor notification service
            networkMonitor.startNotificationService()
            
            android.util.Log.d("MainActivity", "Network Monitor started successfully")
        } catch (e: Exception) {
            // Handle case where Network Monitor is not available
            android.util.Log.w("MainActivity", "Failed to start Network Monitor", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure Network Monitor is running when activity resumes
        // This handles cases where the app was backgrounded and permissions were granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED) {
                startNetworkMonitor()
            }
        } else {
            startNetworkMonitor()
        }
    }
}
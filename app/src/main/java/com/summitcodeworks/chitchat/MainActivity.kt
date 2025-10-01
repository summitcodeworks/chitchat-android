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

/**
 * Main Activity for the ChitChat application.
 * 
 * This is the primary activity that serves as the entry point for the user interface.
 * It handles permission requests, initializes the NetworkMonitor service, and sets up
 * the main navigation structure using Jetpack Compose.
 * 
 * Key responsibilities:
 * - Handle notification permissions for Android 13+ (API level 33+)
 * - Initialize and start the NetworkMonitor service for debugging
 * - Set up the main UI using Jetpack Compose with ChitChatTheme
 * - Manage the navigation flow through ChitChatNavigation
 * - Handle edge-to-edge display configuration
 * 
 * The activity uses Hilt for dependency injection and follows modern Android development
 * practices with Jetpack Compose for UI and proper permission handling.
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    /**
     * Permission launcher for requesting notification permissions on Android 13+.
     * 
     * This launcher handles the result of the notification permission request.
     * Whether the permission is granted or denied, we start the NetworkMonitor
     * service. If permission is denied, the service will work but won't be able
     * to show notifications for network monitoring.
     */
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
    
    /**
     * Called when the activity is first created.
     * 
     * This method performs the following initialization tasks:
     * 1. Enables edge-to-edge display for modern Android UI
     * 2. Requests notification permissions if needed (Android 13+)
     * 3. Starts the NetworkMonitor service for debugging
     * 4. Sets up the main UI using Jetpack Compose with the app's theme
     * 
     * The UI is built using ChitChatTheme and ChitChatNavigation components,
     * which handle the overall app structure and navigation flow.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern Android UI experience
        enableEdgeToEdge()
        
        // Request notification permission for Network Monitor on Android 13+
        requestNotificationPermission()
        
        // Start Network Monitor notification service for debugging capabilities
        startNetworkMonitor()
        
        // Set up the main UI using Jetpack Compose
        setContent {
            ChitChatTheme {
                ChitChatNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    /**
     * Requests notification permission for Android 13+ devices.
     * 
     * Starting from Android 13 (API level 33), apps need explicit permission
     * to post notifications. This method checks the current permission status
     * and requests it if necessary.
     * 
     * For devices running Android 12 and below, no notification permission
     * is required, so we directly start the NetworkMonitor service.
     */
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
                    // Request the permission using the registered launcher
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, no notification permission needed
            startNetworkMonitor()
        }
    }
    
    /**
     * Initializes and starts the NetworkMonitor service.
     * 
     * This method ensures that the NetworkMonitor is properly initialized and
     * starts the notification service for network debugging. The NetworkMonitor
     * allows developers to inspect HTTP requests, responses, and errors in real-time.
     * 
     * The method handles initialization gracefully - if the NetworkMonitor fails
     * to start, it logs a warning but doesn't crash the app, as this is a
     * debugging feature and not essential for core app functionality.
     */
    private fun startNetworkMonitor() {
        try {
            // Ensure Network Monitor is initialized - get existing instance or create new one
            val networkMonitor = NetworkMonitor.getInstance() ?: NetworkMonitor.initialize(this)
            
            // Start Network Monitor notification service for debugging
            networkMonitor.startNotificationService()
            
            android.util.Log.d("MainActivity", "Network Monitor started successfully")
        } catch (e: Exception) {
            // Handle case where Network Monitor is not available or fails to initialize
            android.util.Log.w("MainActivity", "Failed to start Network Monitor", e)
        }
    }
    
    /**
     * Called when the activity resumes from a paused state.
     * 
     * This method ensures that the NetworkMonitor service is still running
     * when the user returns to the app. This is particularly important for
     * handling cases where:
     * - The app was backgrounded and the user granted notification permission
     * - The system may have stopped the NetworkMonitor service
     * - The user returns to the app after being away for a while
     * 
     * The method checks permissions again and restarts the NetworkMonitor
     * service if needed.
     */
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
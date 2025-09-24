package com.summitcodeworks.chitchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.chuckerteam.chucker.Chucker
import com.summitcodeworks.chitchat.presentation.navigation.ChitChatNavigation
import com.summitcodeworks.chitchat.ui.theme.ChitChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configure status bar for white background with black text
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }
        window.statusBarColor = android.graphics.Color.WHITE
        
        // Launch Chucker for network debugging (only in debug builds)
        Chucker.show(this)
        
        setContent {
            ChitChatTheme {
                ChitChatNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
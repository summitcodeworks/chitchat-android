package com.summitcodeworks.chitchat.presentation.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.summitcodeworks.chitchat.R
import com.summitcodeworks.chitchat.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

/**
 * Splash screen for the ChitChat application.
 * 
 * This screen serves as the entry point of the application and handles
 * the initial authentication and profile setup flow. It displays the
 * app logo and performs necessary initialization checks before navigating
 * to the appropriate screen.
 * 
 * Splash screen flow:
 * 1. Display app logo and branding
 * 2. Check user authentication status
 * 3. Verify profile completeness if authenticated
 * 4. Update device token for push notifications
 * 5. Navigate to appropriate screen based on user state
 * 
 * Navigation logic:
 * - If not authenticated: Navigate to OTP authentication
 * - If authenticated but profile incomplete: Navigate to profile setup
 * - If authenticated and profile complete: Navigate to home screen
 * 
 * Key features:
 * - Smooth branding experience with logo display
 * - Authentication state validation
 * - Profile completeness checking
 * - Device token registration for notifications
 * - Automatic navigation based on user state
 * - Loading state management during checks
 * 
 * The splash screen ensures:
 * - Proper app initialization
 * - User authentication verification
 * - Profile setup completion
 * - Device token registration
 * - Smooth transition to main app
 * 
 * @param onNavigateToAuth Callback to navigate to authentication screen
 * @param onNavigateToHome Callback to navigate to home screen
 * @param onNavigateToProfileSetup Callback to navigate to profile setup
 */
@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfileSetup: () -> Unit
) {
    val splashViewModel = hiltViewModel<SplashViewModel>()
    val otpAuthManager = splashViewModel.otpAuthManager
    val isAuthenticated by otpAuthManager.isAuthenticated.collectAsState()
    val currentToken by otpAuthManager.currentToken.collectAsState()
    val hasCompleteProfile by splashViewModel.hasCompleteProfile.collectAsState()

    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds

        // Check if user is properly authenticated with OTP
        if (isAuthenticated && currentToken != null) {
            // Check if user has a complete profile
            splashViewModel.checkProfileCompleteness()
        } else {
            onNavigateToAuth()
        }
    }

    // Navigate based on profile completeness
    LaunchedEffect(hasCompleteProfile) {
        if (hasCompleteProfile != null) {
            if (hasCompleteProfile == true) {
                // Update device token and go to home
                splashViewModel.updateDeviceTokenIfAuthenticated()
                onNavigateToHome()
            } else {
                // Profile incomplete, go to profile setup
                onNavigateToProfileSetup()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CC",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ChitChat",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connect with your world",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

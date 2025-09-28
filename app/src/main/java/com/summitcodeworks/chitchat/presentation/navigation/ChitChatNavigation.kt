package com.summitcodeworks.chitchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// AuthScreen removed - using OtpAuthScreen
import com.summitcodeworks.chitchat.presentation.screen.auth.OtpAuthScreen
import com.summitcodeworks.chitchat.presentation.screen.chat.ChatScreen
import com.summitcodeworks.chitchat.presentation.screen.home.HomeScreen
import com.summitcodeworks.chitchat.presentation.screen.profile.ProfileSetupScreen
import com.summitcodeworks.chitchat.presentation.screen.splash.SplashScreen
import com.summitcodeworks.chitchat.presentation.screen.status.StatusScreen
import com.summitcodeworks.chitchat.presentation.screen.calls.CallsScreen
import com.summitcodeworks.chitchat.presentation.screen.contacts.ContactPickerScreen
import com.summitcodeworks.chitchat.presentation.screen.status.StatusCameraScreen
import com.summitcodeworks.chitchat.presentation.screen.calls.CallContactsScreen
import com.summitcodeworks.chitchat.presentation.screen.demo.ComponentDemoScreen
import com.summitcodeworks.chitchat.presentation.screen.debug.DebugScreen
import com.summitcodeworks.chitchat.presentation.screen.settings.SettingsScreen

@Composable
fun ChitChatNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.OtpAuth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Legacy Auth screen removed - now using OTP authentication
        
        composable(Screen.OtpAuth.route) {
            OtpAuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OtpAuth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { userId ->
                    navController.navigate(Screen.Chat.createRoute(userId))
                },
                onNavigateToContactPicker = {
                    navController.navigate(Screen.ContactPicker.route)
                },
                onNavigateToStatusCamera = {
                    navController.navigate(Screen.StatusCamera.route)
                },
                onNavigateToCallContacts = {
                    navController.navigate(Screen.CallContacts.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.OtpAuth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = Screen.Chat.arguments
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull()
            if (userId != null) {
                ChatScreen(
                    userId = userId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(Screen.Status.route) {
            StatusScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Calls.route) {
            CallsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Demo.route) {
            ComponentDemoScreen()
        }

        composable(Screen.ContactPicker.route) {
            ContactPickerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onContactSelected = { userId ->
                    navController.navigate(Screen.Chat.createRoute(userId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.StatusCamera.route) {
            StatusCameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CallContacts.route) {
            CallContactsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVoiceCall = { userId ->
                    // TODO: Implement voice call
                    navController.popBackStack()
                },
                onVideoCall = { userId ->
                    // TODO: Implement video call
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen()
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.OtpAuth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        }
    }
}

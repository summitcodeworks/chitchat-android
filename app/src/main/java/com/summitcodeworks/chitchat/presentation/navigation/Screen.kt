package com.summitcodeworks.chitchat.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed class defining all navigation routes in the ChitChat application.
 * 
 * This class provides type-safe navigation routes for the entire application,
 * ensuring compile-time safety for navigation operations. Each screen is
 * defined with its route and any required arguments.
 * 
 * Navigation structure:
 * - Authentication flow: Splash -> OTP Auth -> Profile Setup -> Home
 * - Main app screens: Home, Status, Calls, Settings, Search
 * - Feature screens: Chat, Contact Picker, Status Camera, etc.
 * - Development screens: Demo, Debug (for testing and development)
 * 
 * Screen categories:
 * - Core screens: Splash, Home, Settings
 * - Authentication: Auth, OtpAuth, ProfileSetup
 * - Communication: Chat, Status, Calls
 * - Utility: ContactPicker, Search, Debug
 * - Development: Demo, Debug
 * 
 * Parameterized routes:
 * - Chat screen accepts userId parameter for direct messaging
 * - Other screens may be extended with parameters as needed
 * 
 * The sealed class pattern ensures:
 * - Exhaustive when statements for navigation logic
 * - Compile-time route validation
 * - Type-safe parameter passing
 * - Easy maintenance and refactoring
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object OtpAuth : Screen("otp_auth")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object Status : Screen("status")
    object Calls : Screen("calls")
    object ContactPicker : Screen("contact_picker")
    object StatusCamera : Screen("status_camera")
    object CallContacts : Screen("call_contacts")
    object Demo : Screen("demo")
    object Debug : Screen("debug")
    object Settings : Screen("settings")
    object Search : Screen("search")
    
    object Chat : Screen(
        route = "chat/{userId}",
        arguments = listOf(
            navArgument("userId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(userId: Long) = "chat/$userId"
    }
}

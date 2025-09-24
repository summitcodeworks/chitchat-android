package com.summitcodeworks.chitchat.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
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

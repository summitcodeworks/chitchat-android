package com.summitcodeworks.networkmonitor.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
val Primary = Color(0xFF000000) // Black
val Accent = Color(0xFF1DA1F2) // Twitter Blue
val TextPrimary = Color(0xFF000000) // Black
val TextSecondary = Color(0xFF657786) // Dark Gray
val Background = Color(0xFFFFFFFF) // White
val Surface = Color(0xFFF5F8FA) // Extra Light Gray
val Divider = Color(0xFFE1E8ED) // Light Gray
val CustomGreen = Color(0xFF00FF00) // Custom Green

// Dark Mode Colors
val DarkPrimary = Color(0xFFFFFFFF) // White
val DarkAccent = Color(0xFF1DA1F2) // Twitter Blue
val DarkTextPrimary = Color(0xFFFFFFFF) // White
val DarkTextSecondary = Color(0xFFAAB8C2) // Light Gray
val DarkBackground = Color(0xFF000000) // Black
val DarkSurface = Color(0xFF1C2526) // Dark Blue-Gray
val DarkDivider = Color(0xFF657786) // Gray
val DarkCustomGreen = Color(0xFF00FF00) // Custom Green

// HTTP Method Colors
val GetMethodColor = Accent // Blue for GET
val PostMethodColor = CustomGreen // Green for POST
val PutMethodColor = Color(0xFFFF9800) // Orange for PUT
val DeleteMethodColor = Color(0xFFF44336) // Red for DELETE
val PatchMethodColor = Color(0xFF9C27B0) // Purple for PATCH

// HTTP Status Colors
val StatusSuccess = CustomGreen // 2xx
val StatusRedirect = Color(0xFF2196F3) // 3xx - Blue
val StatusClientError = Color(0xFFFF9800) // 4xx - Orange
val StatusServerError = Color(0xFFF44336) // 5xx - Red

// Chart Colors
val ChartColors = listOf(
    CustomGreen,
    Accent,
    PutMethodColor,
    DeleteMethodColor,
    Color(0xFF9E9E9E) // Gray
)

// Legacy Colors (kept for compatibility)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)
val Info = Color(0xFF2196F3)
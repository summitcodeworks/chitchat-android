package com.summitcodeworks.chitchat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryVariant,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = SecondaryVariant,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnPrimary
)

/**
 * Main theme composable for the ChitChat application.
 * 
 * This composable defines the complete Material 3 theme for the application,
 * including color schemes, typography, and status bar configuration. It supports
 * both light and dark themes with consistent branding across the app.
 * 
 * Theme features:
 * - Material 3 design system implementation
 * - Light and dark theme support
 * - Custom color scheme for brand consistency
 * - Typography system with proper text styles
 * - Status bar configuration for modern UI
 * - Dynamic color support (Android 12+) - disabled for branding
 * 
 * Color scheme includes:
 * - Primary colors for main UI elements
 * - Secondary colors for accents and highlights
 * - Surface colors for cards and containers
 * - Error colors for validation and alerts
 * - Background colors for screens and layouts
 * 
 * Status bar configuration:
 * - Automatic status bar color matching
 * - Light/dark status bar content based on theme
 * - Edge-to-edge display support
 * - Proper contrast for accessibility
 * 
 * The theme ensures:
 * - Consistent visual identity across all screens
 * - Proper contrast ratios for accessibility
 * - Modern Material Design 3 aesthetics
 * - Responsive design for different screen sizes
 * - Dark theme support for user preference
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use Android 12+ dynamic colors (disabled for branding)
 * @param content The composable content to be themed
 */
@Composable
fun ChitChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar appearance based on theme
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
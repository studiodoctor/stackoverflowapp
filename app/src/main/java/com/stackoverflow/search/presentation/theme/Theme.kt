/**
 * Project: StackOverflow Search App
 * Author: Manpreet Singh Mall
 * Created: 2026-03-22
 *
 * Description:
 * This file is part of the application and follows MVVM and Clean Architecture principles.
 *
 * Tech Stack:
 * Kotlin, Jetpack Compose, Coroutines, Flow
 */
package com.stackoverflow.search.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Stack Overflow brand colors
val SOrange = Color(0xFFF48024)
val SOrangeLight = Color(0xFFF6954D)
val SOrangeDark = Color(0xFFCC6600)
val SBlue = Color(0xFF0077CC)
val SBlueLight = Color(0xFF0995DD)
val SGreen = Color(0xFF48A868)
val SRed = Color(0xFFDE4F54)
val SGray = Color(0xFF6A737C)
val SLightGray = Color(0xFFF1F2F3)

private val DarkColorScheme = darkColorScheme(
    primary = SOrange,
    onPrimary = Color.White,
    primaryContainer = SOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = SBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF003A6B),
    onSecondaryContainer = Color.White,
    tertiary = SGreen,
    background = Color(0xFF1B1B1B),
    onBackground = Color(0xFFE4E6E7),
    surface = Color(0xFF242424),
    onSurface = Color(0xFFE4E6E7),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF9FA6AD),
    error = SRed,
    outline = Color(0xFF3D3D3D)
)

private val LightColorScheme = lightColorScheme(
    primary = SOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF0E0),
    onPrimaryContainer = SOrangeDark,
    secondary = SBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F0FF),
    onSecondaryContainer = Color(0xFF003A6B),
    tertiary = SGreen,
    background = Color(0xFFF1F2F3),
    onBackground = Color(0xFF232629),
    surface = Color.White,
    onSurface = Color(0xFF232629),
    surfaceVariant = Color(0xFFF8F9F9),
    onSurfaceVariant = SGray,
    error = SRed,
    outline = Color(0xFFD6D9DC)
)

@Composable
fun StackOverflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

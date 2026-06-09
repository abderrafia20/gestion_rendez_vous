package com.example.gestion_rendez_vous.ui.theme

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
    primary = TealPrimary,
    onPrimary = SlateBackgroundLight,
    primaryContainer = TealDark,
    secondary = NavySecondaryContainer,
    onSecondary = NavyDark,
    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    error = ErrorRed,
    onError = SlateSurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SlateSurfaceLight,
    primaryContainer = TealPrimaryContainer,
    secondary = NavySecondary,
    onSecondary = SlateSurfaceLight,
    background = SlateBackgroundLight,
    surface = SlateSurfaceLight,
    error = ErrorRed,
    onError = SlateSurfaceLight
)

@Composable
fun MediSecureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to preserve our premium tailored HSL branding
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

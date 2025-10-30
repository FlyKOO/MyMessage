package com.example.mymessage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.mymessage.MainActivity

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = ColorSchemeDefaults.onPrimary,
    primaryContainer = GreenLight,
    onPrimaryContainer = ColorSchemeDefaults.onPrimaryContainer,
    secondary = ColorSchemeDefaults.secondary,
    onSecondary = ColorSchemeDefaults.onSecondary
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = ColorSchemeDefaults.onPrimary,
    primaryContainer = GreenPrimary,
    onPrimaryContainer = ColorSchemeDefaults.onPrimaryContainer,
    secondary = ColorSchemeDefaults.secondary,
    onSecondary = ColorSchemeDefaults.onSecondary
)

@Composable
fun MyMessageTheme(
    darkTheme: Boolean,
    activity: MainActivity,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    SideEffect {
        val window = activity.window
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !darkTheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

private object ColorSchemeDefaults {
    val onPrimary = androidx.compose.ui.graphics.Color.White
    val onPrimaryContainer = androidx.compose.ui.graphics.Color.Black
    val secondary = androidx.compose.ui.graphics.Color(0xFF4DB6AC)
    val onSecondary = androidx.compose.ui.graphics.Color.Black
}

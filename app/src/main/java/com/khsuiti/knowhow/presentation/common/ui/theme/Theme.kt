package com.khsuiti.knowhow.presentation.common.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.khsuiti.knowhow.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = color2,
    onPrimary = Color.White,
    background = dark,
    onBackground = light,
    surface = Color(0xFF1E2638),
    onSurface = light,
    secondary = color2,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFF2B364E),
    onSurfaceVariant = light,
    outline = light,
    error = error,
    tertiary = submit
)

// В светлой теме color2 и color3 меняются местами
private val LightColorScheme = lightColorScheme(
    primary = color3,             // color3 вместо color2
    onPrimary = Color.White,
    background = light,
    onBackground = dark,
    surface = color2,             // color2 вместо color3
    onSurface = Color.White,
    secondary = color3,           // color3 вместо color2
    onSecondary = Color.White,
    surfaceVariant = color1,
    onSurfaceVariant = color3,
    outline = dark,
    error = error,
    tertiary = submit
)

@Composable
fun AlfaDemobCalendarTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    SystemBarsColor(darkTheme)
    
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

@Composable
fun SystemBarsColor(darkTheme: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
        }
    }
}

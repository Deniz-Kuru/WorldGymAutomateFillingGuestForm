package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val WorldGymColorScheme = darkColorScheme(
    primary = WorldGymRed,
    onPrimary = WorldGymWhite,
    primaryContainer = WorldGymDarkRed,
    onPrimaryContainer = WorldGymWhite,
    
    secondary = WorldGymGray,
    onSecondary = WorldGymWhite,
    secondaryContainer = WorldGymLightGray,
    onSecondaryContainer = WorldGymWhite,
    
    background = WorldGymBlack,
    onBackground = WorldGymWhite,
    
    surface = WorldGymBlack,
    onSurface = WorldGymWhite,
    surfaceVariant = WorldGymGray,
    onSurfaceVariant = WorldGymWhite,
    
    outline = WorldGymRed,
    error = WorldGymRed,
    onError = WorldGymWhite,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to keep the World Gym branding consistent
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Standard parameters suppressed as we enforce World Gym branding (Black/Red/White)
    val colorScheme = when {
        dynamicColor && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> WorldGymColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

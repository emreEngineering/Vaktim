package com.project.vaktim.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldMuted,
    tertiary = GoldLight,
    background = MidnightNavy,
    surface = NavyCard,
    surfaceVariant = NavySurface,
    outline = GlassBorderSoft,
    onPrimary = MidnightNavy,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun VaktimTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = MidnightNavy.copy(alpha = 0.72f).toArgb()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

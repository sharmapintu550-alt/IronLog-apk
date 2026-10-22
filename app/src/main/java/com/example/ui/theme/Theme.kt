package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextWhite,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = TextWhite,
    secondary = ElectricBlueLight,
    onSecondary = AmoledBlack,
    background = AmoledBlack,
    onBackground = TextWhite,
    surface = AmoledBlack,
    onSurface = TextWhite,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder
)

@Composable
fun IronLogTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = AmoledBlack.toArgb()
                it.navigationBarColor = AmoledBlack.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AlAnsariDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = ObsidianBlack,
    primaryContainer = DeepCharcoal,
    onPrimaryContainer = GoldBright,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    secondaryContainer = CrimsonRedDark,
    onSecondaryContainer = PureWhite,
    tertiary = SoftCream,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PureWhite,
    surface = DeepCharcoal,
    onSurface = PureWhite,
    surfaceVariant = ElevatedDark,
    onSurfaceVariant = MutedGray,
    outline = GoldDarkBorder,
    outlineVariant = SubtleDivider,
)

@Composable
fun AlAnsariTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AlAnsariDarkColorScheme,
        typography = AlAnsariTypography,
        content = content,
    )
}

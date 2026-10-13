package com.baburamtechnologies.expensetracker.core.designsystem.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DefaultNeonScheme = darkColorScheme(
    primary = PurplePrimary,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = SurfaceColor,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = ErrorColor
)

@Composable
fun ExpenseTrackerTheme(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    content: @Composable () -> Unit
) {
    val colorScheme = DefaultNeonScheme

    val dimensions = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactDimensions
        WindowWidthSizeClass.Medium -> MediumDimensions
        WindowWidthSizeClass.Expanded -> ExpandedDimensions
        else -> CompactDimensions
    }

    val appTypography = when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactTypography
        WindowWidthSizeClass.Expanded -> ExpandedTypography
        else -> AppTypography()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalAppDimensions provides dimensions,
        LocalAppTypography provides appTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object ExpenseTrackerTheme {
    val dimensions: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimensions.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}

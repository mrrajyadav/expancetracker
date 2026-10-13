package com.baburamtechnologies.expensetracker.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimensions(
    val screenPadding: Dp = 24.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val contentSpacing: Dp = 16.dp,
    val largeSpacing: Dp = 32.dp,
    val extraLargeSpacing: Dp = 40.dp,
    val logoSize: Dp = 220.dp,
    val iconSize: Dp = 24.dp,
    val buttonHeight: Dp = 54.dp,
    val cardRadius: Dp = 12.dp,
    val inputRadius: Dp = 12.dp,
    val maxContentWidth: Dp = 600.dp,
    val maxAuthContentWidth: Dp = 480.dp,
    
    // Standard Spaces
    val Space4: Dp = 4.dp,
    val Space8: Dp = 8.dp,
    val Space12: Dp = 12.dp,
    val Space16: Dp = 16.dp,
    val Space20: Dp = 20.dp,
    val Space24: Dp = 24.dp,
    val Space32: Dp = 32.dp,
    val Space40: Dp = 40.dp,
    
    // Standard Radii
    val Radius8: Dp = 8.dp,
    val Radius12: Dp = 12.dp,
    val Radius16: Dp = 16.dp,
    val Radius20: Dp = 20.dp,
    val Radius24: Dp = 24.dp,
    
    // Standard Icons
    val Icon16: Dp = 16.dp,
    val Icon20: Dp = 20.dp,
    val Icon24: Dp = 24.dp,
    val Icon32: Dp = 32.dp,
    
    // Standard Elevations
    val Elevation2: Dp = 2.dp,
    val Elevation4: Dp = 4.dp,
    val Elevation8: Dp = 8.dp
)

val CompactDimensions = AppDimensions(
    screenPadding = 20.dp,
    paddingSmall = 4.dp,
    paddingMedium = 12.dp,
    paddingLarge = 20.dp,
    contentSpacing = 12.dp,
    largeSpacing = 20.dp,
    extraLargeSpacing = 24.dp,
    logoSize = 130.dp,
    buttonHeight = 50.dp
)

val MediumDimensions = AppDimensions(
    screenPadding = 32.dp,
    paddingSmall = 8.dp,
    paddingMedium = 16.dp,
    paddingLarge = 24.dp,
    contentSpacing = 20.dp,
    largeSpacing = 40.dp,
    extraLargeSpacing = 60.dp,
    logoSize = 150.dp,
    buttonHeight = 56.dp
)

val ExpandedDimensions = AppDimensions(
    screenPadding = 48.dp,
    paddingSmall = 12.dp,
    paddingMedium = 24.dp,
    paddingLarge = 32.dp,
    contentSpacing = 24.dp,
    largeSpacing = 48.dp,
    extraLargeSpacing = 80.dp,
    logoSize = 180.dp,
    buttonHeight = 60.dp
)

val LocalAppDimensions = staticCompositionLocalOf { CompactDimensions }

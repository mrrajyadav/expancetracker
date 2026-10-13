package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Immutable
object BottomBarDefaults {
    object Dimensions {
        val Height = 72.dp
        val IndicatorHeight = 52.dp
        val Radius = 30.dp
        val IndicatorRadius = 26.dp
        val Icon = 26.dp
        val SelectedIcon = 28.dp
        val Margin = 16.dp
    }

    object Elevation {
        val Tonal = 6.dp
        val Shadow = 8.dp
        val IndicatorShadow = 4.dp
    }

    object Animation {
        const val Duration = 250
    }

    @Composable
    @ReadOnlyComposable
    fun containerColor(): Color = CardBg

    @Composable
    @ReadOnlyComposable
    fun outlineColor(): Color = CardStroke

    @Composable
    @ReadOnlyComposable
    fun selectedIconColor(): Color = Color.White

    @Composable
    @ReadOnlyComposable
    fun unselectedIconColor(): Color = TextSecondary

    @Composable
    @ReadOnlyComposable
    fun pillGradient(): Brush = Brush.horizontalGradient(
        colors = listOf(
            AccentPurple,
            PurpleGradientEnd
        )
    )
}

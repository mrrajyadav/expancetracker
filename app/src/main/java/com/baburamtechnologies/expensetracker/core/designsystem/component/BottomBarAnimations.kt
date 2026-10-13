package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Immutable
object BottomBarAnimations {
    val PillTransitionSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val ColorTransitionSpec = tween<Color>(
        durationMillis = BottomBarDefaults.Animation.Duration
    )
    
    val ScaleTransitionSpec = tween<Float>(
        durationMillis = BottomBarDefaults.Animation.Duration
    )
    
    val AlphaTransitionSpec = tween<Float>(
        durationMillis = BottomBarDefaults.Animation.Duration
    )
}

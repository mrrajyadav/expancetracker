package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) BottomBarDefaults.selectedIconColor() else BottomBarDefaults.unselectedIconColor(),
        animationSpec = BottomBarAnimations.ColorTransitionSpec,
        label = "ColorTransition"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = BottomBarAnimations.ScaleTransitionSpec,
        label = "ScaleTransition"
    )

    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.8f,
        animationSpec = BottomBarAnimations.AlphaTransitionSpec,
        label = "AlphaTransition"
    )

    val iconSize = if (selected) BottomBarDefaults.Dimensions.SelectedIcon else BottomBarDefaults.Dimensions.Icon

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No Ripple
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(iconSize)
                .scale(scale)
                .alpha(alpha)
        )
    }
}

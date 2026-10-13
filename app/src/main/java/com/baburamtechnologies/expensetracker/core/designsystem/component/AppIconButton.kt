package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.onBackground,
        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    )
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

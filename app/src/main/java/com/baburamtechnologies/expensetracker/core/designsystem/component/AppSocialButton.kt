package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun AppSocialButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    SecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        leadingIcon = icon
    )
}

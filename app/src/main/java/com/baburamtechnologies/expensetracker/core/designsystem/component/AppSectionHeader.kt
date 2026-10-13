package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = style,
        color = color,
        modifier = modifier.padding(
            horizontal = ExpenseTrackerTheme.dimensions.screenPadding,
            vertical = ExpenseTrackerTheme.dimensions.Space8
        )
    )
}

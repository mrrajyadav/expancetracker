package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ExpenseTrackerTheme.dimensions.maxContentWidth,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

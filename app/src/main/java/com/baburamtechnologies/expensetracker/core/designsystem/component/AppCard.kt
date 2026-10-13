package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = ExpenseTrackerTheme.dimensions.Elevation2,
    shadowElevation: Dp = ExpenseTrackerTheme.dimensions.Elevation4,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = shadowElevation,
        ),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(ExpenseTrackerTheme.dimensions.paddingMedium),
            content = content
        )
    }
}

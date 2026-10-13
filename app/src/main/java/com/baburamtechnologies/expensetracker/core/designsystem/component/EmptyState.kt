package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ExpenseTrackerTheme.dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ExpenseTrackerTheme.dimensions.Space40 * 2),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space24))
        Text(
            text = title,
            style = ExpenseTrackerTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space8))
        Text(
            text = message,
            style = ExpenseTrackerTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space32))
            action()
        }
    }
}

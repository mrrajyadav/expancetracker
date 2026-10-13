package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ExpenseTrackerTheme.dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(ExpenseTrackerTheme.dimensions.Space40),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space16))
        Text(
            text = message,
            style = ExpenseTrackerTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space24))
            SecondaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.width(IntrinsicSize.Min)
            )
        }
    }
}

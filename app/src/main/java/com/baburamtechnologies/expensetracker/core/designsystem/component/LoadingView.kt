package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppCircularProgress()
        if (message != null) {
            Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.Space16))
            Text(
                text = message,
                style = ExpenseTrackerTheme.typography.bodyMedium
            )
        }
    }
}

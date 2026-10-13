package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ExpenseTrackerTheme.dimensions.Radius12),
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ExpenseTrackerTheme.dimensions.buttonHeight),
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        border = border,
        contentPadding = contentPadding
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(ExpenseTrackerTheme.dimensions.Space8))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(ExpenseTrackerTheme.dimensions.Space8))
                    trailingIcon()
                }
            }
        }
    }
}

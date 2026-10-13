package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    isDanger: Boolean = false
) {
    AppDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = dismissText?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = it)
                }
            }
        }
    )
}

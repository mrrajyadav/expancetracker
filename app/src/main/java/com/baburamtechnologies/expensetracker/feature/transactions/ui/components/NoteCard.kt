package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.presentation.AddTransactionUiState

@Composable
fun NoteCard(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Note",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "${note.length} / ${AddTransactionUiState.NOTE_MAX_LENGTH}",
                color = if (note.length >= AddTransactionUiState.NOTE_MAX_LENGTH) AccentRed else TextSecondary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            placeholder = {
                Text(
                    text = "Add details about this transaction...",
                    color = TextSecondary.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = CardStroke,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}

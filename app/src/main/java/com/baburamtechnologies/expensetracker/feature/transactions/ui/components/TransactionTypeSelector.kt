package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, CardStroke, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionTypeButton(
            text = "Income",
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeChange(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
        TransactionTypeButton(
            text = "Expense",
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeChange(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color.Transparent else CardBg,
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) TextWhite else TextSecondary,
        label = "contentColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(AccentPurple, PurpleGradientEnd)
                        )
                    )
                } else {
                    Modifier.background(backgroundColor)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun TransactionTypeSelectorPreview() {
    ExpenseTrackerTheme {
        TransactionTypeSelector(
            selectedType = TransactionType.EXPENSE,
            onTypeChange = {}
        )
    }
}

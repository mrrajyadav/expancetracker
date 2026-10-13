package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Composable
fun AmountCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    onCalculatorClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Amount",
            color = if (error != null) AccentRed else TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (error != null) AccentRed else CardStroke),
            colors = CardDefaults.outlinedCardColors(containerColor = CardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹",
                    color = if (error != null) AccentRed else TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentPurple)
                )
                IconButton(onClick = onCalculatorClick) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator",
                        tint = AccentPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = AccentRed,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
    singleLine: Boolean = false,
    cursorBrush: androidx.compose.ui.graphics.Brush = androidx.compose.ui.graphics.SolidColor(Color.Black)
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = singleLine,
        cursorBrush = cursorBrush,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
        )
    )
}

@Preview
@Composable
fun AmountCardPreview() {
    ExpenseTrackerTheme {
        AmountCard(amount = "1250.00", onAmountChange = {}, onCalculatorClick = {})
    }
}

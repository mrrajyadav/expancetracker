package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateCard(
    dateMillis: Long,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Transaction Date",
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onDateClick() },
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke),
            colors = CardDefaults.outlinedCardColors(containerColor = CardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formattedDate,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

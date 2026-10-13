package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DateRangeHeader(
    selectedMonth: YearMonth,
    customDateRange: Pair<Long, Long>?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCalendarClick: () -> Unit,
    onClearCustomRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = remember(selectedMonth, customDateRange) {
        if (customDateRange != null) {
            val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            "${formatter.format(Date(customDateRange.first))} - ${formatter.format(Date(customDateRange.second))}"
        } else {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            selectedMonth.format(formatter)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left spacer or back button could go here if needed, but for now we match HomeHeaderBar style
        // We'll leave it to the caller to provide leading/trailing icons if they differ
        
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (customDateRange == null) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = TextSecondary)
                }
            } else {
                IconButton(onClick = onClearCustomRange) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Range", tint = AccentRed)
                }
            }
            
            Text(
                text = displayText,
                color = if (customDateRange != null) AccentPurple else TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            if (customDateRange == null) {
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = TextSecondary)
                }
            }
        }

        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(CardBg)
        ) {
            Icon(
                Icons.Default.DateRange, 
                contentDescription = "Calendar", 
                tint = if (customDateRange != null) AccentPurple else TextPrimary
            )
        }
    }
}

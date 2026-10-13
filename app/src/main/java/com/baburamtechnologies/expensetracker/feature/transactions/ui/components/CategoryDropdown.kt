package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Composable
fun CategoryDropdown(
    label: String,
    selectedName: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    emptyPlaceholder: String = "No options available"
) {
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Text(
            text = label,
            color = if (error != null) AccentRed else TextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            Column {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = enabled) { onExpandClick() },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (error != null) AccentRed else if (expanded) AccentPurple else CardStroke),
                    colors = androidx.compose.material3.CardDefaults.outlinedCardColors(containerColor = CardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = if (error != null) AccentRed else AccentPurple
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (enabled && options.isEmpty() && selectedName.contains("Select")) emptyPlaceholder else selectedName,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
                
                CategoryDropdownMenu(
                    expanded = expanded,
                    items = options,
                    selectedItem = selectedName,
                    onItemClick = {
                        onOptionSelected(it)
                        onExpandClick()
                    }
                )
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

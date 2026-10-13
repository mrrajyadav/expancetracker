package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Composable
fun CategoryDropdownMenu(
    expanded: Boolean,
    items: List<String>,
    selectedItem: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .border(1.dp, CardStroke, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            color = CardBg,
            tonalElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(items) { item ->
                    CategoryItem(
                        name = item,
                        selected = item == selectedItem,
                        modifier = Modifier.clickable { onItemClick(item) }
                    )
                }
            }
        }
    }
}

package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SelectedBottomIndicator(
    itemWidth: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(itemWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarDefaults.Dimensions.IndicatorHeight)
                .padding(horizontal = 6.dp)
                .shadow(
                    elevation = BottomBarDefaults.Elevation.IndicatorShadow,
                    shape = RoundedCornerShape(BottomBarDefaults.Dimensions.IndicatorRadius)
                )
                .clip(RoundedCornerShape(BottomBarDefaults.Dimensions.IndicatorRadius))
                .background(BottomBarDefaults.pillGradient())
        )
    }
}

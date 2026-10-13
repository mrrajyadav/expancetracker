package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme

@Composable
fun ElevatedBottomBar(
    items: List<BottomBarItemState>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = BottomBarDefaults.Dimensions.Margin,
                vertical = BottomBarDefaults.Dimensions.Margin
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarDefaults.Dimensions.Height)
                .border(
                    width = 1.dp,
                    color = BottomBarDefaults.outlineColor().copy(alpha = 0.2f),
                    shape = RoundedCornerShape(BottomBarDefaults.Dimensions.Radius)
                ),
            shape = RoundedCornerShape(BottomBarDefaults.Dimensions.Radius),
            color = BottomBarDefaults.containerColor(),
            tonalElevation = BottomBarDefaults.Elevation.Tonal,
            shadowElevation = BottomBarDefaults.Elevation.Shadow
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                val containerWidth = maxWidth
                val itemWidth = containerWidth / items.size
                
                val transition = updateTransition(targetState = selectedIndex, label = "BottomBarTransition")
                
                val indicatorOffset by transition.animateDp(
                    transitionSpec = { BottomBarAnimations.PillTransitionSpec },
                    label = "IndicatorOffset"
                ) { targetIndex ->
                    itemWidth * targetIndex
                }

                // Selected Pill Indicator
                SelectedBottomIndicator(
                    itemWidth = itemWidth,
                    modifier = Modifier.offset(x = indicatorOffset)
                )

                // Navigation Items
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items.forEachIndexed { index, item ->
                        BottomBarItem(
                            icon = item.icon,
                            selected = index == selectedIndex,
                            onClick = { onItemClick(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

data class BottomBarItemState(
    val icon: ImageVector,
    val label: String, // Keeping label in state for accessibility/contentDescription
    val route: String
)

@Preview(showBackground = true, backgroundColor = 0xFF0F0E1D, name = "Phone - Home")
@Composable
fun ElevatedBottomBarPreview() {
    val items = listOf(
        BottomBarItemState(Icons.Default.Home, "Home", "home"),
        BottomBarItemState(Icons.Default.PieChart, "Stats", "stats"),
        BottomBarItemState(Icons.Default.Settings, "Settings", "settings"),
        BottomBarItemState(Icons.Default.Person, "Profile", "profile")
    )
    ExpenseTrackerTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ElevatedBottomBar(
                items = items,
                selectedIndex = 0,
                onItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E1D, name = "Phone - Stats")
@Composable
fun ElevatedBottomBarStatsPreview() {
    val items = listOf(
        BottomBarItemState(Icons.Default.Home, "Home", "home"),
        BottomBarItemState(Icons.Default.PieChart, "Stats", "stats"),
        BottomBarItemState(Icons.Default.Settings, "Settings", "settings"),
        BottomBarItemState(Icons.Default.Person, "Profile", "profile")
    )
    ExpenseTrackerTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ElevatedBottomBar(
                items = items,
                selectedIndex = 1,
                onItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E1D, name = "Tablet", widthDp = 700)
@Composable
fun ElevatedBottomBarTabletPreview() {
    val items = listOf(
        BottomBarItemState(Icons.Default.Home, "Home", "home"),
        BottomBarItemState(Icons.Default.PieChart, "Stats", "stats"),
        BottomBarItemState(Icons.Default.Settings, "Settings", "settings"),
        BottomBarItemState(Icons.Default.Person, "Profile", "profile")
    )
    ExpenseTrackerTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ElevatedBottomBar(
                items = items,
                selectedIndex = 2,
                onItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E1D, name = "Landscape", widthDp = 800, heightDp = 400)
@Composable
fun ElevatedBottomBarLandscapePreview() {
    val items = listOf(
        BottomBarItemState(Icons.Default.Home, "Home", "home"),
        BottomBarItemState(Icons.Default.PieChart, "Stats", "stats"),
        BottomBarItemState(Icons.Default.Settings, "Settings", "settings"),
        BottomBarItemState(Icons.Default.Person, "Profile", "profile")
    )
    ExpenseTrackerTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            ElevatedBottomBar(
                items = items,
                selectedIndex = 3,
                onItemClick = {}
            )
        }
    }
}

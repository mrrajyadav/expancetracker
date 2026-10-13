package com.baburamtechnologies.expensetracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.baburamtechnologies.expensetracker.core.designsystem.component.BottomBarItemState
import com.baburamtechnologies.expensetracker.core.designsystem.component.ElevatedBottomBar

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(NavigationRoutes.HOME, "Home", Icons.Default.Home)
    object Transactions : BottomNavItem(NavigationRoutes.TRANSACTIONS, "Transactions", Icons.AutoMirrored.Filled.List)
    object Stats : BottomNavItem(NavigationRoutes.STATS, "Stats", Icons.Default.PieChart)
    object Profile : BottomNavItem(NavigationRoutes.PROFILE, "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit
) {
    val items = remember {
        listOf(
            BottomNavItem.Home,
            BottomNavItem.Transactions,
            BottomNavItem.Stats,
            BottomNavItem.Profile
        )
    }

    val itemStates = remember(items) {
        items.map {
            BottomBarItemState(
                icon = it.icon,
                label = it.title,
                route = it.route
            )
        }
    }

    val selectedIndex = remember(currentRoute, itemStates) {
        itemStates.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    ElevatedBottomBar(
        items = itemStates,
        selectedIndex = selectedIndex,
        onItemClick = { index ->
            onItemSelected(items[index])
        }
    )
}

package com.baburamtechnologies.expensetracker.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.baburamtechnologies.expensetracker.MainViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.ExpenseTrackerTheme
import com.baburamtechnologies.expensetracker.core.designsystem.component.AppScaffold

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.baburamtechnologies.expensetracker.core.designsystem.theme.AccentPurple
import com.baburamtechnologies.expensetracker.core.designsystem.theme.CardBg
import com.baburamtechnologies.expensetracker.core.designsystem.theme.TextPrimary

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ExpenseTrackerRoot(
    viewModel: MainViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isInitialSetupComplete by viewModel.isInitialSetupComplete.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        NavigationDestination.Home.route,
        NavigationDestination.Transactions.route,
        NavigationDestination.Stats.route,
        NavigationDestination.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    if (isLoggedIn != null) {
        ExpenseTrackerTheme(
            windowWidthSizeClass = windowSizeClass.widthSizeClass
        ) {
            AppScaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = CardBg,
                            contentColor = TextPrimary,
                            actionColor = AccentPurple
                        )
                    }
                },
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavigationBar(
                            currentRoute = currentRoute,
                            onItemSelected = { item ->
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                val startDest = when {
                    isLoggedIn != true -> NavigationDestination.AuthSelection.route
                    !isInitialSetupComplete -> NavigationDestination.RestoreOnboarding.route
                    else -> NavigationDestination.Home.route
                }
                
                NavGraph(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    startDestination = startDest,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

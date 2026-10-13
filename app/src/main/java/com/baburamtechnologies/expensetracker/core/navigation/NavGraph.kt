package com.baburamtechnologies.expensetracker.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baburamtechnologies.expensetracker.feature.auth.presentation.AuthCoordinator
import com.baburamtechnologies.expensetracker.feature.auth.presentation.selection.AuthSelectionScreen
import com.baburamtechnologies.expensetracker.feature.auth.presentation.restore.RestoreOnboardingScreen
import com.baburamtechnologies.expensetracker.feature.auth.presentation.common.AuthSuccessScreen
import com.baburamtechnologies.expensetracker.feature.home.presentation.HomeScreen
import com.baburamtechnologies.expensetracker.feature.transactions.presentation.AddTransactionScreen
import com.baburamtechnologies.expensetracker.feature.transactions.presentation.TransactionsScreen
import com.baburamtechnologies.expensetracker.feature.stats.presentation.StatsScreen
import com.baburamtechnologies.expensetracker.feature.profile.presentation.ProfileScreen
import com.baburamtechnologies.expensetracker.feature.profile.presentation.backup.BackupManagementScreen
import com.baburamtechnologies.expensetracker.feature.settings.presentation.ManageCategoriesScreen
import com.baburamtechnologies.expensetracker.feature.settings.presentation.ManagePaymentModesScreen

import androidx.compose.material3.SnackbarHostState

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    startDestination: String = NavigationRoutes.HOME,
    modifier: Modifier = Modifier
) {
    val coordinator = remember(navController) { AuthCoordinator(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(NavigationDestination.AuthSelection.route) {
            AuthSelectionScreen(
                onNavigateToHome = { coordinator.navigateToHome() },
                onNavigateToSuccess = { message -> coordinator.navigateToSuccess(message) },
                onNavigateToRestore = { coordinator.navigateToRestore() }
            )
        }
        
        composable(
            route = NavigationDestination.AuthSuccess.route,
            arguments = listOf(navArgument("message") { defaultValue = "Operation completed" })
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: ""
            AuthSuccessScreen(
                message = message,
                onAnimationFinished = { coordinator.navigateToHome() }
            )
        }

        composable(NavigationDestination.RestoreOnboarding.route) {
            RestoreOnboardingScreen(
                onNavigateToHome = { coordinator.navigateToHome() }
            )
        }
        
        composable(NavigationDestination.Home.route) {
            HomeScreen(
                onViewAllCategoriesClick = {
                    navController.navigate(NavigationDestination.Stats.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(NavigationDestination.Transactions.route) {
            TransactionsScreen(
                onNavigateToAddEdit = { id -> 
                    navController.navigate(NavigationDestination.AddTransaction.createRoute(id)) 
                }
            )
        }

        composable(NavigationDestination.Stats.route) {
            StatsScreen()
        }

        composable(NavigationDestination.Profile.route) {
            ProfileScreen(
                onManageCategoriesClick = { navController.navigate(NavigationDestination.ManageCategories.route) },
                onManagePaymentModesClick = { navController.navigate(NavigationDestination.ManagePaymentModes.route) },
                onBackupManagementClick = { navController.navigate(NavigationDestination.BackupManagement.route) }
            )
        }

        composable(NavigationDestination.BackupManagement.route) {
            BackupManagementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationDestination.ManageCategories.route) {
            ManageCategoriesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationDestination.ManagePaymentModes.route) {
            ManagePaymentModesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = NavigationDestination.AddTransaction.route,
            arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
        ) {
            AddTransactionScreen(
                onBackClick = { coordinator.navigateBack() }
            )
        }
    }
}

package com.baburamtechnologies.expensetracker.feature.auth.presentation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.baburamtechnologies.expensetracker.core.navigation.NavigationDestination

class AuthCoordinator(private val navController: NavHostController) {
    fun navigateToSelection() {
        navController.navigate(NavigationDestination.AuthSelection.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    fun navigateToSuccess(message: String) {
        navController.navigate(NavigationDestination.AuthSuccess.createRoute(message)) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    fun navigateToRestore() {
        navController.navigate(NavigationDestination.RestoreOnboarding.route)
    }

    fun navigateToHome() {
        navController.navigate(NavigationDestination.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}

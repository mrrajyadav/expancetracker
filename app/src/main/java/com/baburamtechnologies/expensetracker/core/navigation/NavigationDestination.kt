package com.baburamtechnologies.expensetracker.core.navigation

import android.net.Uri

sealed class NavigationDestination(val route: String) {
    object AuthSelection : NavigationDestination(NavigationRoutes.AUTH_SELECTION)
    
    object AuthSuccess : NavigationDestination(NavigationRoutes.AUTH_SUCCESS) {
        fun createRoute(message: String) = "auth_success/${Uri.encode(message)}"
    }

    object RestoreOnboarding : NavigationDestination(NavigationRoutes.RESTORE_ONBOARDING)
    
    object Home : NavigationDestination(NavigationRoutes.HOME)
    object Transactions : NavigationDestination(NavigationRoutes.TRANSACTIONS)
    object Stats : NavigationDestination(NavigationRoutes.STATS)
    object Profile : NavigationDestination(NavigationRoutes.PROFILE)
    object AddTransaction : NavigationDestination(NavigationRoutes.ADD_TRANSACTION) {
        fun createRoute(id: String? = null) = if (id != null) "add_transaction?id=$id" else "add_transaction"
    }
    object ManageCategories : NavigationDestination(NavigationRoutes.MANAGE_CATEGORIES)
    object ManagePaymentModes : NavigationDestination(NavigationRoutes.MANAGE_PAYMENT_MODES)
    object BackupManagement : NavigationDestination("backup_management")
}

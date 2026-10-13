package com.baburamtechnologies.expensetracker.core.security

import com.baburamtechnologies.expensetracker.feature.auth.data.remote.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    /**
     * Observable flow of the current authentication state.
     * Emits true if Authenticated and Verified, false otherwise.
     */
    val isUserLoggedIn: Flow<Boolean?> = authRemoteDataSource.getAuthStatusFlow().map { user ->
        when {
            user == null -> false
            user.providerData.any { it.providerId == "google.com" } -> true
            user.isEmailVerified -> true
            else -> false
        }
    }

    /**
     * Observable flow of the current user's ID.
     * Emits the ID if Authenticated and Verified, or null otherwise.
     */
    val userIdFlow: Flow<String?> = authRemoteDataSource.getAuthStatusFlow().map { user ->
        user?.let {
            val isGoogle = it.providerData.any { p -> p.providerId == "google.com" }
            if (isGoogle || it.isEmailVerified) it.uid else null
        }
    }

    fun getCurrentUserId(): String? {
        val user = authRemoteDataSource.getCurrentUser() ?: return null
        val isGoogle = user.providerData.any { it.providerId == "google.com" }
        return if (isGoogle || user.isEmailVerified) user.uid else null
    }

    /**
     * Checks if there is an active session currently that is verified.
     */
    fun isActiveSession(): Boolean {
        val user = authRemoteDataSource.getCurrentUser() ?: return false
        val isGoogle = user.providerData.any { it.providerId == "google.com" }
        return isGoogle || user.isEmailVerified
    }

    /**
     * Signs out the user and clears the session.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            authRemoteDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

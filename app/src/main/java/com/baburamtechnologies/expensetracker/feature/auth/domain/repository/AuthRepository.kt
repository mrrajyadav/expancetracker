package com.baburamtechnologies.expensetracker.feature.auth.domain.repository

import com.baburamtechnologies.expensetracker.feature.auth.domain.model.User
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun linkCredential(credential: AuthCredential): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun getSessionFlow(): Flow<Boolean?>
    fun isUserLoggedIn(): Boolean
}

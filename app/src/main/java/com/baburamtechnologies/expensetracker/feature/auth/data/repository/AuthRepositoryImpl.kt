package com.baburamtechnologies.expensetracker.feature.auth.data.repository

import com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity
import com.baburamtechnologies.expensetracker.core.database.repository.ProfileRepository
import com.baburamtechnologies.expensetracker.core.auth.GoogleSignInHandler
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.feature.auth.data.remote.AuthRemoteDataSource
import com.baburamtechnologies.expensetracker.feature.auth.data.mapper.toDomainUser
import com.baburamtechnologies.expensetracker.feature.auth.domain.model.User
import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionManager: SessionManager,
    private val googleSignInHandler: GoogleSignInHandler,
    private val profileRepository: ProfileRepository
) : AuthRepository {

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            remoteDataSource.loginWithGoogle(idToken)
            val firebaseUser = remoteDataSource.getCurrentUser()
            if (firebaseUser != null) {
                // Sync profile info from Google Account immediately
                val now = Instant.now().toString()
                val profile = ProfileEntity(
                    id = firebaseUser.uid,
                    fullName = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber,
                    createdAt = now,
                    updatedAt = now
                )
                profileRepository.syncProfile(profile)
                
                Result.success(firebaseUser.toDomainUser())
            } else {
                Result.failure(Exception("Google login successful but user session not found"))
            }
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            remoteDataSource.signOut()
            googleSignInHandler.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            remoteDataSource.reloadUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            remoteDataSource.deleteAccount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    override suspend fun linkCredential(credential: AuthCredential): Result<Unit> {
        return try {
            remoteDataSource.linkCredential(credential)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapAuthError(e))
        }
    }

    override suspend fun getCurrentUser(): User? {
        return remoteDataSource.getCurrentUser()?.toDomainUser()
    }

    override fun getSessionFlow(): Flow<Boolean?> {
        return sessionManager.isUserLoggedIn
    }

    override fun isUserLoggedIn(): Boolean {
        return sessionManager.isActiveSession()
    }

    private fun mapAuthError(e: Exception): Exception {
        if (e is FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_INVALID_CREDENTIALS" -> "Invalid email or password"
                "ERROR_USER_NOT_FOUND" -> "User not found"
                "ERROR_WRONG_PASSWORD" -> "Wrong password"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                "ERROR_REQUIRES_RECENT_LOGIN" -> "This operation requires recent authentication. Please log in again."
                else -> e.localizedMessage ?: "Authentication error occurred"
            }
            return Exception(message)
        }
        return Exception(e.message ?: "An unexpected error occurred")
    }
}

package com.baburamtechnologies.expensetracker.feature.auth.data.remote

import com.baburamtechnologies.expensetracker.core.di.qualifier.IoDispatcher
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthRemoteDataSource {
    suspend fun loginWithGoogle(idToken: String)
    suspend fun signOut()
    suspend fun reloadUser()
    suspend fun deleteAccount()
    suspend fun linkCredential(credential: AuthCredential)
    fun getCurrentUser(): FirebaseUser?
    fun getAuthStatusFlow(): Flow<FirebaseUser?>
    fun isUserLoggedIn(): Boolean
}

class AuthRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRemoteDataSource {

    override suspend fun loginWithGoogle(idToken: String) {
        withContext(ioDispatcher) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
        }
    }

    override suspend fun signOut() {
        withContext(ioDispatcher) {
            try {
                auth.signOut()
            } catch (e: Exception) {
                android.util.Log.e("AuthRemoteDS", "Sign out failed", e)
            }
        }
    }

    override suspend fun reloadUser() {
        withContext(ioDispatcher) {
            auth.currentUser?.reload()?.await()
        }
    }

    override suspend fun deleteAccount() {
        withContext(ioDispatcher) {
            auth.currentUser?.delete()?.await()
        }
    }

    override suspend fun linkCredential(credential: AuthCredential) {
        withContext(ioDispatcher) {
            auth.currentUser?.linkWithCredential(credential)?.await()
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getAuthStatusFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

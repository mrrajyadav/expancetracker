@file:Suppress("DEPRECATION")
package com.baburamtechnologies.expensetracker.core.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.baburamtechnologies.expensetracker.R
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: CurrentActivityProvider
) {
    private val credentialManager = CredentialManager.create(context)

    @Suppress("DEPRECATION")
    private val driveOptions by lazy {
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA))
            .build()
    }

    @Suppress("DEPRECATION")
    fun getDriveAuthIntent(): android.content.Intent {
        val activity = activityProvider.get() ?: throw Exception("Activity not found")
        return com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(activity, driveOptions).signInIntent
    }

    suspend fun signIn(): String? {
        val activity = activityProvider.get() ?: throw Exception("Failed to launch selector: Activity not found")
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )
            handleSignInResult(result)
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Failed to get credential", e)
            val errorMessage = when (e) {
                is androidx.credentials.exceptions.NoCredentialException -> 
                    "No Google accounts found. Please add one in your device settings."
                is androidx.credentials.exceptions.GetCredentialException -> 
                    "Google Sign-In failed: ${e.message}"
                else -> "An unexpected error occurred during Google Sign-In"
            }
            throw Exception(errorMessage)
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): String? {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            return credential.idToken
        }
        return null
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            // Also sign out from legacy client to be thorough
            val activity = activityProvider.get()
            if (activity != null) {
                @Suppress("DEPRECATION")
                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(activity, driveOptions).signOut()
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleSignIn", "Failed to clear credential state", e)
        }
    }
}

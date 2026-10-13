package com.baburamtechnologies.expensetracker.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentUserProvider @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun userId(): String? = auth.currentUser?.uid
    fun email(): String? = auth.currentUser?.email
    fun user(): FirebaseUser? = auth.currentUser
}

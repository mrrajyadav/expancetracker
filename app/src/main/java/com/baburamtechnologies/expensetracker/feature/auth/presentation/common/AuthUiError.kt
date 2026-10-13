package com.baburamtechnologies.expensetracker.feature.auth.presentation.common

sealed interface AuthUiError {
    object NoInternet : AuthUiError
    object WrongPassword : AuthUiError
    object InvalidEmail : AuthUiError
    object UserDisabled : AuthUiError
    object TooManyAttempts : AuthUiError
    object AccountCollision : AuthUiError
    object GoogleCancelled : AuthUiError
    object SessionExpired : AuthUiError
    object WeakPassword : AuthUiError
    data class Unknown(val message: String? = null) : AuthUiError
}

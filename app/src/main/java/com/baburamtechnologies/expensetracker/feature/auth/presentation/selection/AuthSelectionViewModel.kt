package com.baburamtechnologies.expensetracker.feature.auth.presentation.selection

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.auth.GoogleSignInHandler
import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthSelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHandler: GoogleSignInHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthSelectionUiState())
    val uiState: StateFlow<AuthSelectionUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AuthSelectionEffect>()
    val effect: SharedFlow<AuthSelectionEffect> = _effect.asSharedFlow()

    fun onAction(action: AuthSelectionAction) {
        if (_uiState.value.isLoading) return
        
        when (action) {
            AuthSelectionAction.GoogleClick -> loginWithGoogle()
        }
    }

    fun onResume() {
        // Only reset if we are not in the middle of a Google Login or Permission process
        if (_uiState.value.loadingAction != AuthSelectionAction.GoogleClick) {
            _uiState.update { it.copy(isLoading = false, loadingAction = null, errorMessage = null) }
        }
    }

    fun onPermissionResult() {
        viewModelScope.launch {
            _effect.emit(AuthSelectionEffect.NavigateToRestore)
        }
    }

    private fun loginWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingAction = AuthSelectionAction.GoogleClick, errorMessage = null) }
            try {
                val idToken = googleSignInHandler.signIn()
                if (idToken != null) {
                    // Stay in loading state while Firebase authenticates
                    authRepository.loginWithGoogle(idToken).fold(
                        onSuccess = {
                            _effect.emit(AuthSelectionEffect.NavigateToRestore)
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, loadingAction = null, errorMessage = error.message ?: "Google login failed") }
                        }
                    )
                } else {
                    // Cancelled or failed
                    _uiState.update { it.copy(isLoading = false, loadingAction = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, loadingAction = null, errorMessage = e.message ?: "Google Sign-In failed") }
            }
        }
    }
}

data class AuthSelectionUiState(
    val isLoading: Boolean = false,
    val loadingAction: AuthSelectionAction? = null,
    val errorMessage: String? = null
)

sealed class AuthSelectionAction {
    object GoogleClick : AuthSelectionAction()
}

sealed class AuthSelectionEffect {
    object NavigateToHome : AuthSelectionEffect()
    object NavigateToRestore : AuthSelectionEffect()
    data class RequestBackupPermission(val intent: Intent) : AuthSelectionEffect()
    data class NavigateToSuccess(val message: String) : AuthSelectionEffect()
}

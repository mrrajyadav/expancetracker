package com.baburamtechnologies.expensetracker.feature.auth.presentation.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.auth.CurrentUserProvider
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import com.baburamtechnologies.expensetracker.core.backup.domain.RestoreState
import com.baburamtechnologies.expensetracker.core.backup.domain.isActive
import com.baburamtechnologies.expensetracker.core.backup.model.BackupMetadata
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.feature.settings.domain.repository.PaymentModeRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class RestoreOnboardingViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val preferenceManager: PreferenceManager,
    private val paymentModeRepository: PaymentModeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreOnboardingUiState(isLoading = true))
    val uiState: StateFlow<RestoreOnboardingUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RestoreOnboardingEffect>()
    val effect: SharedFlow<RestoreOnboardingEffect> = _effect.asSharedFlow()

    private var timeoutJob: Job? = null
    private val timeoutMillis = 60000L

    init {
        loadBackupMetadata()
        observeRestoreState()
    }

    fun loadBackupMetadata() {
        viewModelScope.launch {
            val email = currentUserProvider.email() ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            backupRepository.checkBackupAvailability(email).fold(
                onSuccess = { metadata ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        backupMetadata = metadata,
                        noBackupFound = metadata == null
                    ) }
                },
                onFailure = { error ->
                    if (error is UserRecoverableAuthIOException) {
                        _effect.emit(RestoreOnboardingEffect.RequestBackupPermission(error.intent))
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to check for backup",
                            noBackupFound = true
                        ) }
                    }
                }
            )
        }
    }

    private fun observeRestoreState() {
        backupRepository.restoreState.onEach { state ->
            _uiState.update { it.copy(
                restoreState = state,
                errorMessage = if (state is RestoreState.Failed) state.message else it.errorMessage
            ) }
            
            if (state.isActive) {
                startTimeoutTimer()
            } else {
                cancelTimeoutTimer()
            }
            
            when (state) {
                is RestoreState.Completed -> {
                    preferenceManager.setInitialSetupComplete(true)
                    _effect.emit(RestoreOnboardingEffect.RestartApp)
                }
                is RestoreState.AuthorizationRequired -> _effect.emit(RestoreOnboardingEffect.RequestBackupPermission(state.intent))
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun startTimeoutTimer() {
        if (timeoutJob?.isActive == true) return
        timeoutJob = viewModelScope.launch {
            delay(timeoutMillis)
            if (_uiState.value.restoreState.isActive) {
                _uiState.update { it.copy(
                    errorMessage = "Restore operation timed out. Please check your connection and try again."
                ) }
            }
        }
    }

    private fun cancelTimeoutTimer() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    fun onPermissionResult() {
        loadBackupMetadata()
    }

    fun onRestoreConfirm() {
        viewModelScope.launch {
            val email = currentUserProvider.email() ?: return@launch
            val uid = currentUserProvider.userId() ?: return@launch
            _uiState.update { it.copy(errorMessage = null) }
            backupRepository.restore(email, uid)
        }
    }

    fun onContinueWithoutBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            backupRepository.clearLocalData().onSuccess {
                preferenceManager.setInitialSetupComplete(true)
                paymentModeRepository.seedDefaultPaymentModes()
                _effect.emit(RestoreOnboardingEffect.NavigateToHome)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to clear local data") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimeoutTimer()
    }
}

data class RestoreOnboardingUiState(
    val isLoading: Boolean = false,
    val noBackupFound: Boolean = false,
    val backupMetadata: BackupMetadata? = null,
    val restoreState: RestoreState = RestoreState.Idle,
    val errorMessage: String? = null
)

sealed class RestoreOnboardingEffect {
    object NavigateToHome : RestoreOnboardingEffect()
    object RestartApp : RestoreOnboardingEffect()
    data class RequestBackupPermission(val intent: android.content.Intent) : RestoreOnboardingEffect()
}

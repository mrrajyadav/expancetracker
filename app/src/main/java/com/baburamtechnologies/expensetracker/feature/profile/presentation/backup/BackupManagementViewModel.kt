package com.baburamtechnologies.expensetracker.feature.profile.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.auth.CurrentUserProvider
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupRepository
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupState
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.core.sync.BackupScheduleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupManagementViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val backupScheduleManager: BackupScheduleManager,
    private val preferenceManager: PreferenceManager,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupManagementUiState())
    val uiState: StateFlow<BackupManagementUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<BackupManagementEffect>()
    val effect: SharedFlow<BackupManagementEffect> = _effect.asSharedFlow()

    init {
        observeSettings()
        observeBackupStates()
    }

    private fun observeSettings() {
        val uid = currentUserProvider.userId() ?: return
        combine(
            preferenceManager.lastBackupTime(uid),
            preferenceManager.autoBackupEnabled(uid)
        ) { lastBackupTime, autoBackupEnabled ->
            BackupManagementUiState(
                lastBackupTime = lastBackupTime ?: "Never",
                isAutoBackupEnabled = autoBackupEnabled
            )
        }
        .onEach { state -> _uiState.value = state }
        .launchIn(viewModelScope)
    }

    private fun observeBackupStates() {
        backupRepository.backupState.onEach { state ->
            _uiState.update { it.copy(currentBackupState = state) }
            when (state) {
                is BackupState.Completed -> _effect.emit(BackupManagementEffect.ShowMessage("Backup successful"))
                is BackupState.Failed -> _effect.emit(BackupManagementEffect.ShowMessage("Backup failed: ${state.message}"))
                is BackupState.AuthorizationRequired -> _effect.emit(BackupManagementEffect.LaunchIntent(state.intent))
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun onAutoBackupToggled(enabled: Boolean) {
        viewModelScope.launch {
            val uid = currentUserProvider.userId() ?: return@launch
            preferenceManager.setAutoBackupEnabled(uid, enabled)
            updateSchedule(enabled)
        }
    }

    private suspend fun updateSchedule(enabled: Boolean) {
        if (enabled) {
            backupScheduleManager.schedulePeriodicBackup()
        } else {
            backupScheduleManager.cancelPeriodicBackup()
        }
    }

    fun startManualBackup() {
        viewModelScope.launch {
            val email = currentUserProvider.email() ?: return@launch
            val uid = currentUserProvider.userId() ?: return@launch
            backupRepository.backup(email, uid)
        }
    }
}

data class BackupManagementUiState(
    val lastBackupTime: String = "Never",
    val isAutoBackupEnabled: Boolean = false,
    val currentBackupState: BackupState = BackupState.Idle
)

sealed class BackupManagementEffect {
    data class ShowMessage(val message: String) : BackupManagementEffect()
    object RestartApp : BackupManagementEffect()
    data class LaunchIntent(val intent: android.content.Intent) : BackupManagementEffect()
}

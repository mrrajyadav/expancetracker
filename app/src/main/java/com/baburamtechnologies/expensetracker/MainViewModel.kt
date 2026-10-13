package com.baburamtechnologies.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.sync.BackupScheduleManager
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.feature.auth.domain.usecase.GetSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val backupScheduleManager: BackupScheduleManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isInitialSetupComplete = MutableStateFlow(false)
    val isInitialSetupComplete = _isInitialSetupComplete.asStateFlow()

    init {
        checkSession()
        observeSetupStatus()
    }

    private fun checkSession() {
        viewModelScope.launch {
            combine(
                getSessionUseCase(),
                preferenceManager.isInitialSetupComplete
            ) { sessionExists, setupComplete ->
                sessionExists to setupComplete
            }.collectLatest { (sessionExists, setupComplete) ->
                _isLoggedIn.value = sessionExists
                _isInitialSetupComplete.value = setupComplete
                
                if (sessionExists == true && setupComplete) {
                    // Trigger immediate backup check ONLY if setup is fully complete
                    backupScheduleManager.scheduleOneTimeBackup()
                }
            }
        }
    }

    private fun observeSetupStatus() {
        viewModelScope.launch {
            preferenceManager.isInitialSetupComplete.collectLatest { complete ->
                _isInitialSetupComplete.value = complete
            }
        }
    }
}

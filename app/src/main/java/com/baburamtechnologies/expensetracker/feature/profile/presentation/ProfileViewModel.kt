package com.baburamtechnologies.expensetracker.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.auth.CurrentUserProvider
import com.baburamtechnologies.expensetracker.core.datastore.PreferenceManager
import com.baburamtechnologies.expensetracker.feature.auth.domain.repository.AuthRepository
import com.baburamtechnologies.expensetracker.feature.profile.domain.usecase.GetProfileUseCase
import com.baburamtechnologies.expensetracker.feature.profile.domain.usecase.UpdateProfileUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetTransactionCountUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetTransactionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getTransactionSummaryUseCase: GetTransactionSummaryUseCase,
    private val getTransactionCountUseCase: GetTransactionCountUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val preferenceManager: PreferenceManager,
    private val currentUserProvider: CurrentUserProvider,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        observeActiveData()
        observeProfile()
        observeSettings()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeActiveData() {
        val (start, end) = getMonthRange()
        combine(
            getTransactionSummaryUseCase(start, end),
            getTransactionCountUseCase()
        ) { summary, count ->
            summary to count
        }
            .onEach { (summary, count) ->
                _uiState.update { it.copy(
                    totalBalance = summary.totalBalance,
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpense,
                    savings = summary.totalIncome - summary.totalExpense,
                    transactionCount = count
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun getMonthRange(): Pair<String, String> {
        val now = YearMonth.now()
        val start = now.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString()
        val end = now.atEndOfMonth().atTime(23, 59, 59, 999_999_999).toInstant(ZoneOffset.UTC).toString()
        return start to end
    }

    private fun observeProfile() {
        getProfileUseCase()
            .onEach { profile ->
                val firebaseUser = currentUserProvider.user()
                
                // Auto-create local profile if it doesn't exist but we are logged in
                if (profile == null && firebaseUser != null) {
                    val now = Instant.now().toString()
                    val newProfile = com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity(
                        id = firebaseUser.uid,
                        fullName = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        phone = firebaseUser.phoneNumber,
                        createdAt = now,
                        updatedAt = now
                    )
                    updateProfileUseCase(newProfile)
                    return@onEach
                }

                val memberSinceFormatted = try {
                    // Try Firebase metadata first
                    firebaseUser?.metadata?.creationTimestamp?.let { timestamp ->
                        if (timestamp > 0) {
                            val instant = Instant.ofEpochMilli(timestamp)
                            val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
                                .withZone(ZoneId.systemDefault())
                            formatter.format(instant)
                        } else null
                    } ?: profile?.createdAt?.let { dateStr ->
                        val instant = try {
                            Instant.parse(dateStr)
                        } catch (e: Exception) {
                            try {
                                java.time.LocalDate.parse(dateStr.substringBefore("T"))
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                            } catch (e2: Exception) { null }
                        }
                        
                        instant?.let {
                            val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
                                .withZone(ZoneId.systemDefault())
                            formatter.format(it)
                        }
                    } ?: "---"
                } catch (e: Exception) {
                    "---"
                }

                _uiState.update { it.copy(
                    name = profile?.fullName?.takeIf { it.isNotBlank() } ?: firebaseUser?.displayName ?: "User",
                    email = profile?.email?.takeIf { it.isNotBlank() } ?: firebaseUser?.email ?: "",
                    phone = profile?.phone?.takeIf { it.isNotBlank() } ?: firebaseUser?.phoneNumber ?: "",
                    profileImageUrl = firebaseUser?.photoUrl?.toString(),
                    memberSince = memberSinceFormatted,
                    isLoading = false
                ) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        combine(
            preferenceManager.lastBackupTime,
            preferenceManager.autoBackupEnabled
        ) { lastBackup, autoEnabled ->
            _uiState.update { it.copy(
                lastBackupTime = lastBackup ?: "Never",
                isAutoBackupEnabled = autoEnabled
            ) }
        }.launchIn(viewModelScope)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.deleteAccount().onSuccess {
                _effect.emit(ProfileEffect.ShowMessage("Account deleted successfully"))
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { error ->
                _effect.emit(ProfileEffect.ShowMessage("Deletion failed: ${error.message}"))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            val userId = currentUserProvider.userId() ?: return@launch
            val currentProfile = _uiState.value
            
            // Get existing profile to preserve createdAt
            val existingProfile = getProfileUseCase().firstOrNull()
            
            val profileEntity = com.baburamtechnologies.expensetracker.core.database.model.ProfileEntity(
                id = userId,
                fullName = name,
                email = currentProfile.email,
                phone = phone,
                createdAt = existingProfile?.createdAt ?: Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )
            
            // 1. Update Firebase Display Name if possible
            currentUserProvider.user()?.let { user ->
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates)
            }

            // 2. Save locally
            updateProfileUseCase(profileEntity).onSuccess {
                _effect.emit(ProfileEffect.ShowMessage("Profile updated successfully"))
            }.onFailure { error ->
                _effect.emit(ProfileEffect.ShowMessage("Failed to save: ${error.message}"))
            }
        }
    }
}

sealed class ProfileEffect {
    object RestartApp : ProfileEffect()
    data class ShowMessage(val message: String) : ProfileEffect()
}

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val savings: Double = 0.0,
    val transactionCount: Int = 0,
    val memberSince: String = "---",
    val lastBackupTime: String = "Never",
    val profileImageUrl: String? = null,
    val isAutoBackupEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

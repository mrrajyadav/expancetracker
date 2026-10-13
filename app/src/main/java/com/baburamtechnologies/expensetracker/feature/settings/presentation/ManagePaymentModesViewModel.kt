package com.baburamtechnologies.expensetracker.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.settings.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagePaymentModesViewModel @Inject constructor(
    private val getPaymentModesUseCase: GetPaymentModesUseCase,
    private val addPaymentModeUseCase: AddPaymentModeUseCase,
    private val updatePaymentModeUseCase: UpdatePaymentModeUseCase,
    private val setPaymentModeActiveUseCase: SetPaymentModeActiveUseCase,
    private val deletePaymentModeUseCase: DeletePaymentModeUseCase,
    private val getTransactionCountUseCase: GetPaymentModeTransactionCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagePaymentModesUiState())
    val uiState: StateFlow<ManagePaymentModesUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ManagePaymentModesEffect>()
    val effect = _effect.asSharedFlow()

    init {
        observePaymentModes()
    }

    private fun observePaymentModes() {
        getPaymentModesUseCase()
            .onEach { modes ->
                _uiState.update { it.copy(
                    activeModes = modes.filter { m -> m.isActive },
                    inactiveModes = modes.filter { m -> !m.isActive },
                    isLoading = false
                ) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun addPaymentMode(name: String) {
        viewModelScope.launch {
            addPaymentModeUseCase(name)
        }
    }

    fun updatePaymentMode(paymentMode: PaymentMode) {
        viewModelScope.launch {
            updatePaymentModeUseCase(paymentMode)
        }
    }

    fun archivePaymentMode(id: String) {
        viewModelScope.launch {
            setPaymentModeActiveUseCase(id, false)
        }
    }

    fun restorePaymentMode(id: String) {
        viewModelScope.launch {
            setPaymentModeActiveUseCase(id, true)
        }
    }

    fun deletePaymentMode(id: String) {
        viewModelScope.launch {
            val count = getTransactionCountUseCase(id)
            if (count > 0) {
                _effect.emit(ManagePaymentModesEffect.ShowDeleteBlocked(count))
            } else {
                _effect.emit(ManagePaymentModesEffect.ShowDeleteConfirmation(id))
            }
        }
    }

    fun confirmDelete(id: String) {
        viewModelScope.launch {
            deletePaymentModeUseCase(id).onSuccess {
                _effect.emit(ManagePaymentModesEffect.ShowMessage("Payment mode deleted"))
            }.onFailure { error ->
                _effect.emit(ManagePaymentModesEffect.ShowMessage(error.message ?: "Failed to delete"))
            }
        }
    }
}

sealed class ManagePaymentModesEffect {
    data class ShowMessage(val message: String) : ManagePaymentModesEffect()
    data class ShowDeleteBlocked(val transactionCount: Int) : ManagePaymentModesEffect()
    data class ShowDeleteConfirmation(val id: String) : ManagePaymentModesEffect()
}

data class ManagePaymentModesUiState(
    val activeModes: List<PaymentMode> = emptyList(),
    val inactiveModes: List<PaymentMode> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

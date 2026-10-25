package com.baburamtechnologies.expensetracker.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.security.SessionManager
import com.baburamtechnologies.expensetracker.core.util.MoneyUtils
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode
import com.baburamtechnologies.expensetracker.feature.settings.domain.usecase.GetActivePaymentModesUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Transaction
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getCategoriesByTypeAndLevelUseCase: GetCategoriesByTypeAndLevelUseCase,
    private val getCategoriesByParentUseCase: GetCategoriesByParentUseCase,
    private val getActivePaymentModesUseCase: GetActivePaymentModesUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val editTransactionUseCase: EditTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event = _event.asSharedFlow()

    init {
        observeData()
        loadExistingTransaction()
    }

    private fun loadExistingTransaction() {
        transactionId?.let { id ->
            viewModelScope.launch {
                val transactionWithCategory = getTransactionByIdUseCase(id)
                transactionWithCategory?.let { item ->
                    val t = item.transaction
                    _uiState.update { it.copy(
                        amount = MoneyUtils.paiseToInputString(t.amount),
                        type = t.type,
                        selectedLevel1Category = item.categoryLevel1,
                        selectedLevel2Category = item.categoryLevel2,
                        selectedLevel3Category = item.categoryLevel3,
                        selectedPaymentMode = item.paymentMode,
                        dateMillis = Instant.parse(t.date).toEpochMilli(),
                        note = t.note ?: "",
                        isEditMode = true,
                        originalCreatedAt = t.createdAt
                    ) }
                    
                    // Trigger loading sub-categories without resetting selections
                    loadLevel2Categories(item.categoryLevel1.id)
                    item.categoryLevel2.let { loadLevel3Categories(it.id) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        getActivePaymentModesUseCase()
            .onEach { modes -> 
                _uiState.update { state ->
                    state.copy(paymentModes = modes) 
                } 
                validateForm()
            }
            .launchIn(viewModelScope)

        _uiState.map { it.type }.distinctUntilChanged()
            .flatMapLatest { type -> getCategoriesByTypeAndLevelUseCase(type, 1) }
            .onEach { categories -> 
                _uiState.update { it.copy(level1Categories = categories) }
                validateForm()
            }
            .launchIn(viewModelScope)
    }

    fun onAmountChange(value: String) {
        if (value.count { it == '.' } > 1) return
        if (value.any { !it.isDigit() && it != '.' }) return
        if (value.contains('.') && value.substringAfter('.').length > 2) return

        _uiState.update { 
            it.copy(
                amount = value, 
                fieldErrors = it.fieldErrors.toMutableMap().apply { remove(AddTransactionUiState.FIELD_AMOUNT) }
            ) 
        }
        validateForm()
    }

    fun onTypeChange(type: TransactionType) {
        if (_uiState.value.type == type) return
        _uiState.update { it.copy(
            type = type,
            selectedLevel1Category = null,
            selectedLevel2Category = null,
            selectedLevel3Category = null,
            level1Categories = emptyList(),
            level2Categories = emptyList(),
            level3Categories = emptyList(),
            fieldErrors = emptyMap()
        ) }
        validateForm()
    }

    fun onLevel1CategoryChange(category: Category) {
        _uiState.update { it.copy(
            selectedLevel1Category = category,
            selectedLevel2Category = null,
            selectedLevel3Category = null,
            level2Categories = emptyList(),
            level3Categories = emptyList(),
            fieldErrors = it.fieldErrors.toMutableMap().apply { remove(AddTransactionUiState.FIELD_CATEGORY_L1) }
        ) }
        loadLevel2Categories(category.id)
    }

    private var level2Job: kotlinx.coroutines.Job? = null
    private fun loadLevel2Categories(parentId: String) {
        level2Job?.cancel()
        level2Job = viewModelScope.launch {
            getCategoriesByParentUseCase(parentId).collectLatest { categories ->
                _uiState.update { it.copy(level2Categories = categories) }
                validateForm()
            }
        }
    }

    fun onLevel2CategoryChange(category: Category) {
        _uiState.update { it.copy(
            selectedLevel2Category = category,
            selectedLevel3Category = null,
            level3Categories = emptyList(),
            fieldErrors = it.fieldErrors.toMutableMap().apply { remove(AddTransactionUiState.FIELD_CATEGORY_L2) }
        ) }
        loadLevel3Categories(category.id)
    }

    private var level3Job: kotlinx.coroutines.Job? = null
    private fun loadLevel3Categories(parentId: String) {
        level3Job?.cancel()
        level3Job = viewModelScope.launch {
            getCategoriesByParentUseCase(parentId).collectLatest { categories ->
                _uiState.update { it.copy(level3Categories = categories) }
                validateForm()
            }
        }
    }

    fun onLevel3CategoryChange(category: Category) {
        _uiState.update { it.copy(selectedLevel3Category = category) }
        validateForm()
    }

    fun onPaymentModeChange(mode: PaymentMode) {
        _uiState.update { it.copy(
            selectedPaymentMode = mode,
            fieldErrors = it.fieldErrors.toMutableMap().apply { remove(AddTransactionUiState.FIELD_PAYMENT_MODE) }
        ) }
        validateForm()
    }

    fun onNoteChange(value: String) {
        if (value.length > AddTransactionUiState.NOTE_MAX_LENGTH) return
        _uiState.update { it.copy(note = value) }
    }

    fun onDateChange(millis: Long) {
        val selectedDate = Instant.ofEpochMilli(millis)
        val now = Instant.now()
        
        if (selectedDate.isAfter(now)) {
            _uiState.update { it.copy(showInvalidDateDialog = true) }
            return
        }
        
        _uiState.update { it.copy(dateMillis = millis, showDatePicker = false) }
        validateForm()
    }

    fun dismissInvalidDateDialog() {
        _uiState.update { it.copy(showInvalidDateDialog = false) }
    }

    fun onShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    private fun validateForm() {
        val state = _uiState.value
        val parsedPaise = MoneyUtils.parseAmountToPaise(state.amount)
        val isValid = parsedPaise != null && parsedPaise > 0L &&
                state.selectedLevel1Category != null &&
                state.selectedLevel2Category != null &&
                state.selectedPaymentMode != null
        
        _uiState.update { it.copy(isFormValid = isValid) }
    }

    fun onSaveClick() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        
        val errors = mutableMapOf<String, String>()
        val parsedPaise = MoneyUtils.parseAmountToPaise(state.amount)
        
        if (parsedPaise == null || parsedPaise <= 0L) errors[AddTransactionUiState.FIELD_AMOUNT] = "Enter valid positive amount (max 2 decimals)"
        if (state.selectedLevel1Category == null) errors[AddTransactionUiState.FIELD_CATEGORY_L1] = "Required"
        if (state.selectedLevel2Category == null) errors[AddTransactionUiState.FIELD_CATEGORY_L2] = "Required"
        if (state.selectedPaymentMode == null) errors[AddTransactionUiState.FIELD_PAYMENT_MODE] = "Required"

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isConfirmationDialogOpen = true, errorMessage = null) }
    }

    fun onConfirmSave() {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        val userId = sessionManager.getCurrentUserId() ?: return
        val amountInPaise = MoneyUtils.parseAmountToPaise(state.amount) ?: return

        val now = Instant.now().toString()
        val transactionDate = Instant.ofEpochMilli(state.dateMillis).toString()
        
        val transaction = Transaction(
            id = transactionId ?: UUID.randomUUID().toString(),
            userId = userId,
            amount = amountInPaise,
            type = state.type,
            categoryLevel1Id = state.selectedLevel1Category!!.id,
            categoryLevel2Id = state.selectedLevel2Category!!.id,
            categoryLevel3Id = state.selectedLevel3Category?.id,
            paymentModeId = state.selectedPaymentMode!!.id,
            note = state.note.takeIf { it.isNotBlank() },
            date = transactionDate,
            createdAt = if (transactionId != null) state.originalCreatedAt ?: now else now,
            updatedAt = now
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isConfirmationDialogOpen = false) }
            val result = if (transactionId != null) {
                editTransactionUseCase(transaction)
            } else {
                addTransactionUseCase(transaction)
            }
            
            result.fold(
                onSuccess = {
                    resetForm()
                    _event.emit(AddTransactionEvent.SaveSuccess)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Failed to save", isLoading = false) }
                }
            )
        }
    }

    private fun resetForm() {
        _uiState.update { 
            AddTransactionUiState(
                level1Categories = it.level1Categories,
                paymentModes = it.paymentModes,
                dateMillis = System.currentTimeMillis()
            )
        }
    }

    fun onDismissConfirmation() {
        _uiState.update { it.copy(isConfirmationDialogOpen = false) }
    }
}

sealed class AddTransactionEvent {
    object SaveSuccess : AddTransactionEvent()
}

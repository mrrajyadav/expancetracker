package com.baburamtechnologies.expensetracker.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.date.DateManager
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionSummary
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionWithCategory
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.DeleteTransactionUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetTransactionSummaryUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionSummaryUseCase: GetTransactionSummaryUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val dateManager: DateManager
) : ViewModel() {

    private val _selectedFilterType = MutableStateFlow<TransactionType?>(null)
    private val _transactionToReview = MutableStateFlow<TransactionWithCategory?>(null)
    private val _uiState = MutableStateFlow(TransactionsUiState())
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionsUiState> = combine(
        dateManager.selectedMonth,
        dateManager.customDateRange,
        _selectedFilterType,
        _transactionToReview,
        _uiState
    ) { month, customRange, filterType, reviewItem, currentUiState ->
        val (start, end) = if (customRange != null) {
            val s = Instant.ofEpochMilli(customRange.first).toString()
            val e = Instant.ofEpochMilli(customRange.second).atZone(ZoneOffset.UTC)
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999)
                .toInstant().toString()
            s to e
        } else {
            getMonthRange(month)
        }

        combine(
            getTransactionsUseCase(start, end),
            getTransactionSummaryUseCase(start, end)
        ) { transactions, summary ->
            val filteredTransactions = if (filterType != null) {
                transactions.filter { it.transaction.type == filterType }
            } else {
                transactions
            }
            currentUiState.copy(
                transactions = filteredTransactions,
                summary = summary,
                isLoading = false,
                selectedMonth = month,
                customDateRange = customRange,
                selectedFilterType = filterType,
                transactionToReview = reviewItem
            )
        }.onStart {
            emit(currentUiState.copy(
                isLoading = true,
                selectedMonth = month,
                customDateRange = customRange,
                selectedFilterType = filterType,
                transactionToReview = reviewItem
            ))
        }
    }.flatMapLatest { it }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    private fun getMonthRange(month: YearMonth): Pair<String, String> {
        val start = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString()
        val end = month.atEndOfMonth().atTime(23, 59, 59, 999_999_999).toInstant(ZoneOffset.UTC).toString()
        return start to end
    }

    fun nextMonth() = dateManager.nextMonth()

    fun previousMonth() = dateManager.previousMonth()

    fun onDateRangeSelected(startMillis: Long, endMillis: Long) {
        dateManager.updateCustomDateRange(startMillis to endMillis)
    }

    fun clearCustomDateRange() = dateManager.clearCustomDateRange()

    fun onShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun onFilterTypeChange(type: TransactionType?) {
        _selectedFilterType.value = type
    }

    fun resetToCurrentMonth() {
        _selectedFilterType.value = null
        dateManager.resetToCurrentMonth()
    }

    fun onTransactionClick(transaction: TransactionWithCategory) {
        _transactionToReview.value = transaction
    }

    fun dismissReviewDialog() {
        _transactionToReview.value = null
    }

    fun onDeleteClick(transaction: TransactionWithCategory) {
        _uiState.update { it.copy(transactionToDelete = transaction) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(transactionToDelete = null) }
    }

    fun confirmDelete() {
        val transaction = _uiState.value.transactionToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, transactionToDelete = null) }
            deleteTransactionUseCase(transaction.transaction.id).onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Failed to delete transaction", isLoading = false) }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class TransactionsUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val summary: TransactionSummary = TransactionSummary(0.0, 0.0, 0.0),
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val customDateRange: Pair<Long, Long>? = null,
    val selectedFilterType: TransactionType? = null,
    val showDatePicker: Boolean = false,
    val errorMessage: String? = null,
    val transactionToReview: TransactionWithCategory? = null,
    val transactionToDelete: TransactionWithCategory? = null
)

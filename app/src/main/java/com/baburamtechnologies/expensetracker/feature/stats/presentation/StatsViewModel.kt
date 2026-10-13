package com.baburamtechnologies.expensetracker.feature.stats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baburamtechnologies.expensetracker.core.date.DateManager
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.CategorySummary
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionSummary
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetCategoryBreakdownUseCase
import com.baburamtechnologies.expensetracker.feature.transactions.domain.usecase.GetTransactionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getTransactionSummaryUseCase: GetTransactionSummaryUseCase,
    private val getCategoryBreakdownUseCase: GetCategoryBreakdownUseCase,
    private val dateManager: DateManager
) : ViewModel() {

    private val _showDatePicker = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = combine(
        dateManager.selectedMonth,
        dateManager.customDateRange,
        _showDatePicker
    ) { month, customRange, showPicker ->
        Triple(month, customRange, showPicker)
    }.flatMapLatest { (month, customRange, showPicker) ->
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
            getTransactionSummaryUseCase(start, end),
            getCategoryBreakdownUseCase(start, end)
        ) { summary, breakdown ->
            StatsUiState(
                summary = summary,
                categoryBreakdown = breakdown,
                isLoading = false,
                selectedMonth = month,
                customDateRange = customRange,
                showDatePicker = showPicker
            )
        }.onStart {
            emit(StatsUiState(
                isLoading = true,
                selectedMonth = month,
                customDateRange = customRange,
                showDatePicker = showPicker
            ))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
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
        _showDatePicker.value = show
    }

    fun resetToCurrentMonth() = dateManager.resetToCurrentMonth()
}

data class StatsUiState(
    val summary: TransactionSummary = TransactionSummary(0.0, 0.0, 0.0),
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now(),
    val customDateRange: Pair<Long, Long>? = null,
    val showDatePicker: Boolean = false,
    val errorMessage: String? = null
)

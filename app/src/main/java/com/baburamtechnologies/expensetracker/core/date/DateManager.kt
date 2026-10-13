package com.baburamtechnologies.expensetracker.core.date

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateManager @Inject constructor() {
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange.asStateFlow()

    fun updateMonth(month: YearMonth) {
        _customDateRange.value = null
        _selectedMonth.value = month
    }

    fun nextMonth() {
        _customDateRange.value = null
        _selectedMonth.update { it.plusMonths(1) }
    }

    fun previousMonth() {
        _customDateRange.value = null
        _selectedMonth.update { it.minusMonths(1) }
    }

    fun updateCustomDateRange(range: Pair<Long, Long>) {
        _customDateRange.value = range
    }

    fun clearCustomDateRange() {
        _customDateRange.value = null
    }

    fun resetToCurrentMonth() {
        _customDateRange.value = null
        _selectedMonth.value = YearMonth.now()
    }
}

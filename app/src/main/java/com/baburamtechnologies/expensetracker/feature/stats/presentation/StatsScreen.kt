package com.baburamtechnologies.expensetracker.feature.stats.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import com.baburamtechnologies.expensetracker.feature.transactions.ui.components.DateRangeHeader
import com.baburamtechnologies.expensetracker.feature.transactions.ui.resolveCategoryName
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.resetToCurrentMonth()
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                ResponsiveContainer(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    DateRangeHeader(
                        selectedMonth = uiState.selectedMonth,
                        customDateRange = uiState.customDateRange,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                        onCalendarClick = { viewModel.onShowDatePicker(true) },
                        onClearCustomRange = viewModel::clearCustomDateRange,
                        modifier = Modifier.padding(horizontal = ExpenseTrackerTheme.dimensions.screenPadding, vertical = 8.dp)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                ResponsiveContainer(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = ExpenseTrackerTheme.dimensions.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(ExpenseTrackerTheme.dimensions.paddingMedium)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        if (uiState.isLoading && uiState.summary.totalIncome == 0.0 && uiState.summary.totalExpense == 0.0 && !pullToRefreshState.isRefreshing) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AccentPurple)
                                }
                            }
                        } else {
                            // Summary Cards Row
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(ExpenseTrackerTheme.dimensions.paddingSmall)) {
                                    MiniStatCard(Modifier.weight(1f), "Income", "₹${uiState.summary.totalIncome}", Icons.Default.Savings, Color(0xFF10B981))
                                    MiniStatCard(Modifier.weight(1f), "Expense", "₹${uiState.summary.totalExpense}", Icons.Default.AccountBalanceWallet, Color(0xFFEF4444))
                                    MiniStatCard(Modifier.weight(1f), "Savings", "₹${uiState.summary.totalBalance}", Icons.Default.Diamond, Color(0xFF8B5CF6))
                                }
                            }

                            // Spending by Category
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Spending by Category", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        if (uiState.categoryBreakdown.isEmpty()) {
                                            Text("No data available", color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                                        } else {
                                            val totalExpense = uiState.categoryBreakdown.sumOf { it.totalAmount }
                                            uiState.categoryBreakdown.forEach { category ->
                                                val progress = if (totalExpense > 0) (category.totalAmount / totalExpense).toFloat() else 0f
                                                val percentage = "%.1f%%".format(progress * 100)
                                                
                                                val icon = Icons.Default.GridView
                                                val color = AccentPurple
                                                val iconBg = DarkBgStart

                                                CategorySpendingRow(
                                                    name = category.categoryName.resolveCategoryName(),
                                                    amount = "₹${category.totalAmount}",
                                                    percent = percentage,
                                                    progress = progress,
                                                    color = color,
                                                    icon = icon,
                                                    iconBg = iconBg
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.paddingLarge))
                        }
                    }
                }

                if (pullToRefreshState.isRefreshing || pullToRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = CardBg,
                        contentColor = AccentPurple
                    )
                }
            }
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        
        LaunchedEffect(dateRangePickerState.selectedEndDateMillis) {
            val start = dateRangePickerState.selectedStartDateMillis
            val end = dateRangePickerState.selectedEndDateMillis
            if (start != null && end != null) {
                viewModel.onDateRangeSelected(start, end)
                viewModel.onShowDatePicker(false)
            }
        }

        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.onDateRangeSelected(start, end)
                            viewModel.onShowDatePicker(false)
                        }
                    }
                ) {
                    Text("OK", color = AccentPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
                headline = { 
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                        Text("${formatter.format(Date(start))} - ${formatter.format(Date(end))}", modifier = Modifier.padding(16.dp))
                    } else {
                        Text("Select range", modifier = Modifier.padding(16.dp))
                    }
                },
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    containerColor = CardBg,
                    selectedDayContainerColor = AccentPurple,
                    todayDateBorderColor = AccentPurple,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = Color.White
                )
            )
        }
    }
}

@Composable
fun MiniStatCard(
    modifier: Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = TextSecondary, fontSize = 10.sp)
            Text(amount, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun CategorySpendingRow(name: String, amount: String, percent: String, progress: Float, color: Color, icon: ImageVector, iconBg: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFF2D264A)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = percent, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Preview
@Composable
fun StatsScreenPreview() {
    StatsScreen()
}

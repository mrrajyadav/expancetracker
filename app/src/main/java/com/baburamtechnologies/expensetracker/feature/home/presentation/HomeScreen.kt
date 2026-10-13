package com.baburamtechnologies.expensetracker.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.CategorySummary
import com.baburamtechnologies.expensetracker.feature.transactions.ui.components.DateRangeHeader
import com.baburamtechnologies.expensetracker.feature.transactions.ui.resolveCategoryName
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onViewAllCategoriesClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

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
                    HomeHeaderBar(
                        uiState = uiState,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                        onCalendarClick = { viewModel.onShowDatePicker(true) },
                        onClearCustomRange = viewModel::clearCustomDateRange,
                        modifier = Modifier.padding(horizontal = ExpenseTrackerTheme.dimensions.screenPadding, vertical = 8.dp)
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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

                        if (uiState.isLoading && !pullToRefreshState.isRefreshing) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AccentPurple)
                                }
                            }
                        } else {
                            item {
                                val periodText = remember(uiState.selectedMonth, uiState.customDateRange) {
                                    uiState.customDateRange?.let { range ->
                                        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                                        "${formatter.format(Date(range.first))} - ${formatter.format(Date(range.second))}"
                                    } ?: if (uiState.selectedMonth == YearMonth.now()) {
                                        "This Month"
                                    } else {
                                        uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
                                    }
                                }
                                TotalExpenseCard(
                                    amount = "₹${uiState.summary.totalExpense}",
                                    period = periodText
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SummaryStatCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Income",
                                        amount = "₹${uiState.summary.totalIncome}",
                                        icon = Icons.Default.AccountBalanceWallet,
                                        iconBg = Color(0xFF16A34A)
                                    )
                                    SummaryStatCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Balance",
                                        amount = "₹${uiState.summary.totalBalance}",
                                        icon = Icons.Default.AccountBalance,
                                        iconBg = Color(0xFF6366F1)
                                    )
                                }
                            }

                            item {
                                CategoryBreakdownCard(
                                    onViewAllClick = onViewAllCategoriesClick,
                                    categories = uiState.categoryBreakdown
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.paddingMedium)) }
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
private fun HomeHeaderBar(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCalendarClick: () -> Unit,
    onClearCustomRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(CardBg)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
        }

        DateRangeHeader(
            selectedMonth = uiState.selectedMonth,
            customDateRange = uiState.customDateRange,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onCalendarClick = onCalendarClick,
            onClearCustomRange = onClearCustomRange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TotalExpenseCard(amount: String, period: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Expense", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(amount, color = Color(0xFFF87171), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2D264A))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("• $period", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentPurple, PurpleGradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = TextPrimary)
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    icon: ImageVector,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(amount, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    onViewAllClick: () -> Unit,
    categories: List<CategorySummary>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Category Breakdown", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onViewAllClick) {
                    Text("View All", color = AccentPurple)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (categories.isEmpty()) {
                Text("No data available", color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                val totalExpense = categories.sumOf { it.totalAmount }
                categories.take(3).forEach { category ->
                    val progress = if (totalExpense > 0) (category.totalAmount / totalExpense).toFloat() else 0f
                    val percentage = "%.1f%%".format(progress * 100)
                    
                    val icon = Icons.Default.GridView
                    val color = AccentPurple
                    val iconBg = CardBg

                    CategoryProgressRow(
                        name = category.categoryName.resolveCategoryName(),
                        amount = "₹${category.totalAmount}",
                        percent = percentage,
                        progress = progress,
                        progressColor = color,
                        icon = icon,
                        iconBg = iconBg
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(
    name: String,
    amount: String,
    percent: String,
    progress: Float,
    progressColor: Color,
    icon: ImageVector,
    iconBg: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(amount, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = Color(0xFF2D264A),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Text(percent, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

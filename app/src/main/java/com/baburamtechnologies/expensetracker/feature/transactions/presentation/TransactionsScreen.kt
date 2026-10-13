package com.baburamtechnologies.expensetracker.feature.transactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionWithCategory
import com.baburamtechnologies.expensetracker.feature.transactions.ui.components.DateRangeHeader
import com.baburamtechnologies.expensetracker.feature.transactions.ui.components.ReviewTransactionDialog
import com.baburamtechnologies.expensetracker.feature.transactions.ui.displayName
import com.baburamtechnologies.expensetracker.feature.transactions.ui.getDisplayName
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

data class TransactionItemModel(
    val id: String,
    val title: String,
    val dateTime: String,
    val amount: String,
    val isIncome: Boolean,
    val icon: ImageVector,
    val iconBg: Color,
    val original: TransactionWithCategory
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateToAddEdit: (String?) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var showFilterMenu by remember { mutableStateOf(false) }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.resetToCurrentMonth()
            pullToRefreshState.endRefresh()
        }
    }

    val transactionsList = uiState.transactions.map { item ->
        val isIncome = item.transaction.type == TransactionType.INCOME
        TransactionItemModel(
            id = item.transaction.id,
            title = item.categoryLevel1.getDisplayName(context),
            dateTime = item.transaction.date,
            amount = "${if (isIncome) "+" else "-"} ₹${item.transaction.amount}",
            isIncome = isIncome,
            icon = if (isIncome) Icons.Default.Savings else Icons.Default.AccountBalanceWallet,
            iconBg = if (isIncome) Color(0xFF10B981) else Color(0xFF8B5CF6),
            original = item
        )
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToAddEdit(null) },
                    containerColor = AccentPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Transaction", fontWeight = FontWeight.Bold) }
                )
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

                        // Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("↑ Income", color = AccentGreen, fontSize = 12.sp)
                                        Text("₹${uiState.summary.totalIncome}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column {
                                        Text("↓ Expense", color = AccentRed, fontSize = 12.sp)
                                        Text("₹${uiState.summary.totalExpense}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column {
                                        Text("Balance", color = TextSecondary, fontSize = 12.sp)
                                        Text("₹${uiState.summary.totalBalance}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }

                        // Title and Filter
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Transactions", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Box {
                                    OutlinedButton(
                                        onClick = { showFilterMenu = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardBg)
                                    ) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, tint = if (uiState.selectedFilterType != null) AccentPurple else TextSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(uiState.selectedFilterType?.name ?: "All", color = TextPrimary, fontSize = 12.sp)
                                    }
                                    DropdownMenu(
                                        expanded = showFilterMenu,
                                        onDismissRequest = { showFilterMenu = false },
                                        modifier = Modifier.background(CardBg)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All", color = TextPrimary) },
                                            onClick = {
                                                viewModel.onFilterTypeChange(null)
                                                showFilterMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Income", color = TextPrimary) },
                                            onClick = {
                                                viewModel.onFilterTypeChange(TransactionType.INCOME)
                                                showFilterMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Expense", color = TextPrimary) },
                                            onClick = {
                                                viewModel.onFilterTypeChange(TransactionType.EXPENSE)
                                                showFilterMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Transactions List
                        if (uiState.isLoading && transactionsList.isEmpty() && !pullToRefreshState.isRefreshing) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AccentPurple)
                                }
                            }
                        } else if (transactionsList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("No transactions for this period", color = TextSecondary)
                                }
                            }
                        } else {
                            items(transactionsList) { item ->
                                TransactionCardRow(
                                    item = item,
                                    onEditClick = { viewModel.onTransactionClick(item.original) },
                                    onDeleteClick = { viewModel.onDeleteClick(item.original) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Clearance for FAB
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

    // Delete Confirmation Dialog
    uiState.transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Delete Transaction", color = TextPrimary) },
            text = { Text("Are you sure you want to delete this transaction?", color = TextSecondary) },
            containerColor = CardBg,
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Yes", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("No", color = TextSecondary)
                }
            }
        )
    }

    // Review Transaction Dialog
    uiState.transactionToReview?.let { transaction ->
        ReviewTransactionDialog(
            amount = transaction.transaction.amount.toString(),
            type = transaction.transaction.type.name,
            categoryL1 = transaction.categoryLevel1.displayName(),
            categoryL2 = transaction.categoryLevel2.displayName(),
            categoryL3 = transaction.categoryLevel3?.displayName() ?: "None",
            paymentMode = transaction.paymentMode?.name ?: "Deleted Mode",
            note = transaction.transaction.note ?: "",
            dateMillis = Instant.parse(transaction.transaction.date).toEpochMilli(),
            onConfirm = viewModel::dismissReviewDialog,
            onEdit = {
                viewModel.dismissReviewDialog()
                onNavigateToAddEdit(transaction.transaction.id)
            },
            onDismiss = viewModel::dismissReviewDialog
        )
    }
}

@Composable
fun TransactionCardRow(
    item: TransactionItemModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(item.dateTime, color = TextSecondary, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.amount,
                    color = if (item.isIncome) AccentGreen else AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C2746))
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentPurple, modifier = Modifier.size(14.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C2746))
                            .clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionsScreenPreview() {
    TransactionsScreen()
}

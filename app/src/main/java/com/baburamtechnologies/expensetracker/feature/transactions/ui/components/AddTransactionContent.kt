package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.feature.transactions.presentation.AddTransactionUiState
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.ui.displayName
import com.baburamtechnologies.expensetracker.feature.transactions.ui.getDisplayName
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode

@Composable
fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onLevel1CategoryChange: (Category) -> Unit,
    onLevel2CategoryChange: (Category) -> Unit,
    onLevel3CategoryChange: (Category) -> Unit,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var l1Expanded by remember { mutableStateOf(false) }
    var l2Expanded by remember { mutableStateOf(false) }
    var l3Expanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Amount
        item {
            AmountCard(
                amount = uiState.amount,
                onAmountChange = onAmountChange,
                onCalculatorClick = { /* Optional */ },
                error = uiState.fieldErrors[AddTransactionUiState.FIELD_AMOUNT]
            )
        }

        // 2. Transaction Type (Income / Expense)
        item {
            TransactionTypeSelector(
                selectedType = uiState.type,
                onTypeChange = onTypeChange
            )
        }

        // 3. Category Level 1
        item {
            CategoryDropdown(
                label = "Level 1 Category",
                selectedName = uiState.selectedLevel1Category?.displayName() ?: "Select Level 1",
                options = uiState.level1Categories.map { it.getDisplayName(context) },
                onOptionSelected = { name ->
                    uiState.level1Categories.find { it.getDisplayName(context) == name }?.let { onLevel1CategoryChange(it) }
                },
                expanded = l1Expanded,
                onExpandClick = {
                    l1Expanded = !l1Expanded
                    if (l1Expanded) {
                        l2Expanded = false
                        l3Expanded = false
                        paymentExpanded = false
                    }
                },
                error = uiState.fieldErrors[AddTransactionUiState.FIELD_CATEGORY_L1]
            )
        }

        // 4. Category Level 2
        item {
            CategoryDropdown(
                label = "Level 2 Category",
                selectedName = uiState.selectedLevel2Category?.displayName() ?: "Select Level 2",
                options = uiState.level2Categories.map { it.getDisplayName(context) },
                onOptionSelected = { name ->
                    uiState.level2Categories.find { it.getDisplayName(context) == name }?.let { onLevel2CategoryChange(it) }
                },
                expanded = l2Expanded,
                onExpandClick = {
                    l2Expanded = !l2Expanded
                    if (l2Expanded) {
                        l1Expanded = false
                        l3Expanded = false
                        paymentExpanded = false
                    }
                },
                enabled = uiState.selectedLevel1Category != null,
                error = uiState.fieldErrors[AddTransactionUiState.FIELD_CATEGORY_L2]
            )
        }

        // 5. Category Level 3 (Optional - Only shown if sub-categories exist)
        if (uiState.selectedLevel2Category != null && uiState.level3Categories.isNotEmpty()) {
            item {
                CategoryDropdown(
                    label = "Level 3 Category (Optional)",
                    selectedName = uiState.selectedLevel3Category?.displayName() ?: "Select Level 3",
                    options = uiState.level3Categories.map { it.getDisplayName(context) },
                    onOptionSelected = { name ->
                        uiState.level3Categories.find { it.getDisplayName(context) == name }
                            ?.let { onLevel3CategoryChange(it) }
                    },
                    expanded = l3Expanded,
                    onExpandClick = {
                        l3Expanded = !l3Expanded
                        if (l3Expanded) {
                            l1Expanded = false
                            l2Expanded = false
                            paymentExpanded = false
                        }
                    },
                    enabled = true
                )
            }
        }

        // 6. NOTE (Now after Categories)
        item {
            NoteCard(
                note = uiState.note,
                onNoteChange = onNoteChange
            )
        }

        // 7. Payment Mode
        item {
            CategoryDropdown(
                label = "Payment Mode",
                selectedName = uiState.selectedPaymentMode?.name ?: "Select Payment Mode",
                options = uiState.paymentModes.map { it.name },
                onOptionSelected = { name ->
                    uiState.paymentModes.find { it.name == name }?.let { onPaymentModeChange(it) }
                },
                expanded = paymentExpanded,
                onExpandClick = {
                    paymentExpanded = !paymentExpanded
                    if (paymentExpanded) {
                        l1Expanded = false
                        l2Expanded = false
                        l3Expanded = false
                    }
                },
                error = uiState.fieldErrors[AddTransactionUiState.FIELD_PAYMENT_MODE],
                emptyPlaceholder = "No payment modes available"
            )
        }

        // 8. DATE (Bottom)
        item {
            DateCard(
                dateMillis = uiState.dateMillis,
                onDateClick = onDateClick
            )
        }

        // Spacer for bottom button
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

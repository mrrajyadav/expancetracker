package com.baburamtechnologies.expensetracker.feature.transactions.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.ui.components.*
import com.baburamtechnologies.expensetracker.feature.transactions.ui.displayName
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is AddTransactionEvent.SaveSuccess) {
                showSuccessDialog = true
            }
        }
    }

    BackHandler(enabled = uiState.isLoading) {
        // Do nothing while loading
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Transaction" else "Add Transaction",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (uiState.isLoading) TextSecondary.copy(alpha = 0.5f) else TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            SaveTransactionButton(
                onClick = viewModel::onSaveClick,
                isLoading = uiState.isLoading,
                enabled = uiState.isFormValid
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
    ) { paddingValues ->
        AddTransactionContent(
            uiState = uiState,
            onAmountChange = viewModel::onAmountChange,
            onTypeChange = viewModel::onTypeChange,
            onLevel1CategoryChange = viewModel::onLevel1CategoryChange,
            onLevel2CategoryChange = viewModel::onLevel2CategoryChange,
            onLevel3CategoryChange = viewModel::onLevel3CategoryChange,
            onPaymentModeChange = viewModel::onPaymentModeChange,
            onNoteChange = viewModel::onNoteChange,
            onDateClick = { viewModel.onShowDatePicker(true) },
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                }) {
                    Text("OK", color = AccentPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = AccentPurple,
                    todayContentColor = AccentPurple,
                    todayDateBorderColor = AccentPurple
                )
            )
        }
    }

    // Invalid Date Dialog
    if (uiState.showInvalidDateDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissInvalidDateDialog,
            title = { Text("Invalid Date", color = TextPrimary) },
            text = { Text("You can't add a future transaction.", color = TextSecondary) },
            containerColor = CardBg,
            confirmButton = {
                TextButton(onClick = viewModel::dismissInvalidDateDialog) {
                    Text("OK", color = AccentPurple)
                }
            }
        )
    }

    if (uiState.isConfirmationDialogOpen) {
        ReviewTransactionDialog(
            amount = uiState.amount,
            type = uiState.type.name,
            categoryL1 = uiState.selectedLevel1Category?.displayName() ?: "",
            categoryL2 = uiState.selectedLevel2Category?.displayName() ?: "",
            categoryL3 = uiState.selectedLevel3Category?.displayName() ?: "None",
            paymentMode = uiState.selectedPaymentMode?.name ?: "",
            note = uiState.note,
            dateMillis = uiState.dateMillis,
            onConfirm = viewModel::onConfirmSave,
            onEdit = viewModel::onDismissConfirmation,
            onDismiss = viewModel::onDismissConfirmation
        )
    }

    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                onBackClick()
            }
        )
    }
}

@Preview
@Composable
fun AddTransactionScreenPreview() {
    AddTransactionScreen()
}

package com.baburamtechnologies.expensetracker.feature.settings.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.settings.domain.model.PaymentMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePaymentModesScreen(
    onBackClick: () -> Unit,
    viewModel: ManagePaymentModesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var modeToEdit by remember { mutableStateOf<PaymentMode?>(null) }
    
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }
    var deleteBlockedCount by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ManagePaymentModesEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ManagePaymentModesEffect.ShowDeleteConfirmation -> {
                    deleteConfirmId = effect.id
                }
                is ManagePaymentModesEffect.ShowDeleteBlocked -> {
                    deleteBlockedCount = effect.transactionCount
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
    ) {
        ResponsiveContainer {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Payment Modes", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = AccentPurple,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Mode")
                    }
                },
                containerColor = Color.Transparent
            ) { padding ->
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { SectionHeader("Active Accounts") }
                        
                        items(uiState.activeModes) { mode ->
                            PaymentModeRow(
                                mode = mode,
                                onEdit = { modeToEdit = it },
                                onDelete = { viewModel.deletePaymentMode(mode.id) },
                                onArchive = { viewModel.archivePaymentMode(mode.id) }
                            )
                        }

                        if (uiState.inactiveModes.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            item { SectionHeader("Inactive Payment Modes") }
                            
                            items(uiState.inactiveModes) { mode ->
                                InactivePaymentModeRow(
                                    mode = mode,
                                    onRestore = { viewModel.restorePaymentMode(mode.id) },
                                    onDelete = { viewModel.deletePaymentMode(mode.id) }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditPaymentModeDialog(
            title = "Add Payment Mode",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addPaymentMode(name)
                showAddDialog = false
            }
        )
    }

    modeToEdit?.let { mode ->
        AddEditPaymentModeDialog(
            title = "Edit Payment Mode",
            initialName = mode.name,
            onDismiss = { modeToEdit = null },
            onConfirm = { name ->
                viewModel.updatePaymentMode(mode.copy(name = name))
                modeToEdit = null
            }
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("Delete Account?", color = TextPrimary) },
            text = { Text("This will permanently remove the account. This cannot be undone.", color = TextSecondary) },
            containerColor = CardBg,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDelete(id)
                        deleteConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    deleteBlockedCount?.let { count ->
        AlertDialog(
            onDismissRequest = { deleteBlockedCount = null },
            title = { Text("Deletion Blocked", color = TextPrimary) },
            text = { 
                Text(
                    text = "This account has $count historical transactions associated with it (including deleted ones). To maintain your financial history and database integrity, this account cannot be permanently deleted. Please use 'Archive' to hide it instead.",
                    color = TextSecondary
                ) 
            },
            containerColor = CardBg,
            confirmButton = {
                Button(
                    onClick = { deleteBlockedCount = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text("I Understand")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AccentPurple,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun PaymentModeRow(
    mode: PaymentMode,
    onEdit: (PaymentMode) -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(44.dp).background(AccentPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(mode.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Row {
                IconButton(onClick = { onEdit(mode) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentPurple, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onArchive) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun InactivePaymentModeRow(
    mode: PaymentMode,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(44.dp).background(TextSecondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(mode.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Archived", color = TextSecondary, fontSize = 12.sp)
                }
            }
            
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Unarchive, contentDescription = "Restore", tint = AccentPurple, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AddEditPaymentModeDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary) },
        containerColor = CardBg,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkBgStart,
                        unfocusedContainerColor = DarkBgStart,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Confirm", color = AccentPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

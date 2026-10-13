package com.baburamtechnologies.expensetracker.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.Category
import com.baburamtechnologies.expensetracker.feature.transactions.domain.model.TransactionType
import com.baburamtechnologies.expensetracker.feature.transactions.ui.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    onBackClick: () -> Unit,
    viewModel: ManageCategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
    ) {
        ResponsiveContainer {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Manage Categories", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                },
                floatingActionButton = {
                    if (uiState.selectedLevel2 != null) {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = AccentPurple,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Level 3")
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    // Type Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBg),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TransactionType.entries.forEach { type ->
                            val isSelected = uiState.selectedType == type
                            TextButton(
                                onClick = { viewModel.onTypeChange(type) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (isSelected) AccentPurple else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else TextSecondary
                                )
                            ) {
                                Text(type.name)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level 1 Selector
                    CategoryDropdown(
                        label = "Level 1 Category",
                        categories = uiState.level1Categories,
                        selectedCategory = uiState.selectedLevel1,
                        onCategorySelect = { viewModel.onLevel1Select(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level 2 Selector
                    CategoryDropdown(
                        label = "Level 2 Category",
                        categories = uiState.level2Categories,
                        selectedCategory = uiState.selectedLevel2,
                        onCategorySelect = { viewModel.onLevel2Select(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.selectedLevel2 != null) {
                        Text(
                            "Level 3 Sub-categories",
                            color = AccentPurple,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.level3Categories) { category ->
                                CategoryRow(
                                    category = category,
                                    onEdit = { categoryToEdit = it },
                                    onDelete = { viewModel.deleteCategory(it.id) },
                                    onToggleActive = { viewModel.toggleCategoryActive(it) }
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select Level 1 & 2 to manage Level 3", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditCategoryDialog(
            title = "Add Sub-category",
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.addLevel3Category(it)
                showAddDialog = false
            }
        )
    }

    categoryToEdit?.let { category ->
        AddEditCategoryDialog(
            title = "Edit Sub-category",
            initialName = category.displayName(),
            onDismiss = { categoryToEdit = null },
            onConfirm = { 
                viewModel.updateCategory(category, it)
                categoryToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    label: String,
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory?.displayName() ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor = CardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardBg)
            ) {
                categories.forEach { category ->
                    val nameText = if (category.isActive) category.displayName() else "${category.displayName()} (Archived)"
                    DropdownMenuItem(
                        text = { Text(nameText, color = if (category.isActive) TextPrimary else TextSecondary) },
                        onClick = {
                            onCategorySelect(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onToggleActive: (Category) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val nameText = if (category.isActive) category.displayName() else "${category.displayName()} (Archived)"
            Text(
                nameText,
                color = if (category.isActive) TextPrimary else TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Row {
                if (category.isActive) {
                    IconButton(onClick = { onEdit(category) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentPurple)
                    }
                    if (!category.isSystem) {
                        IconButton(onClick = { onDelete(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                        }
                    }
                } else {
                    IconButton(onClick = { onToggleActive(category) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restore", tint = AccentPurple)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditCategoryDialog(
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
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Category Name", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkBgStart,
                    unfocusedContainerColor = DarkBgStart,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
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

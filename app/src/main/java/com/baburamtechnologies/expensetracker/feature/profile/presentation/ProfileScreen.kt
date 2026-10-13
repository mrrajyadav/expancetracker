package com.baburamtechnologies.expensetracker.feature.profile.presentation

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.core.designsystem.component.ResponsiveContainer
import com.baburamtechnologies.expensetracker.feature.profile.presentation.component.*

import androidx.compose.ui.res.stringResource
import com.baburamtechnologies.expensetracker.R

@Composable
fun ProfileScreen(
    onManageCategoriesClick: () -> Unit = {},
    onManagePaymentModesClick: () -> Unit = {},
    onBackupManagementClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.RestartApp -> {
                    (context as? Activity)?.recreate()
                }
                is ProfileEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = ExpenseTrackerTheme.dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(ExpenseTrackerTheme.dimensions.paddingMedium)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.paddingMedium))
                        Text(
                            text = stringResource(R.string.profile_title),
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        ProfileHeader(
                            uiState = uiState,
                            onEditProfileClick = { showEditProfileSheet = true }
                        )
                    }

                    item {
                        FinancialOverviewSection(uiState = uiState)
                    }

                    item {
                        SettingsSectionTitle(stringResource(R.string.management))
                        SettingsCardContainer {
                            SettingsOptionRow(
                                title = stringResource(R.string.manage_categories),
                                subtitle = stringResource(R.string.manage_categories_desc),
                                icon = Icons.Default.Category
                            ) {
                                onManageCategoriesClick()
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = CardStroke, thickness = 0.5.dp)
                            SettingsOptionRow(
                                title = stringResource(R.string.manage_payment_modes),
                                subtitle = stringResource(R.string.manage_payment_modes_desc),
                                icon = Icons.Default.AccountBalanceWallet
                            ) {
                                onManagePaymentModesClick()
                            }
                        }
                    }

                    item {
                        SettingsSectionTitle(stringResource(R.string.data))
                        SettingsCardContainer {
                            SettingsOptionRow(
                                title = stringResource(R.string.backup_data),
                                subtitle = "Last backup: ${uiState.lastBackupTime}",
                                icon = Icons.Default.CloudUpload
                            ) {
                                onBackupManagementClick()
                            }
                        }
                    }

                    item {
                        SettingsSectionTitle("Support")
                        SettingsCardContainer {
                            SettingsOptionRow("Privacy Policy", "Read our privacy policy", Icons.Default.Security)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = CardStroke, thickness = 0.5.dp)
                            SettingsOptionRow("Rate App", "Help us improve", Icons.Default.Star)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::logout,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardBg,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AccentRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        SettingsSectionTitle(stringResource(R.string.danger_zone))
                        SettingsCardContainer {
                            SettingsOptionRow(stringResource(R.string.delete_account), "Permanently delete your account", Icons.Default.Delete, isDangerous = true) {
                                showDeleteConfirmDialog = true
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(ExpenseTrackerTheme.dimensions.paddingLarge))
                    }
                }
            }
        }
    }

    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            uiState = uiState,
            onDismiss = { showEditProfileSheet = false },
            onSave = { name, phone ->
                viewModel.updateProfile(name, phone)
                showEditProfileSheet = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = { 
                viewModel.deleteAccount()
                showDeleteConfirmDialog = false
            }
        )
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}

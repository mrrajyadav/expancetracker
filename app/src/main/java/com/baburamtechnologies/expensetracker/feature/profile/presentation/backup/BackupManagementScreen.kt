package com.baburamtechnologies.expensetracker.feature.profile.presentation.backup

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.backup.domain.BackupState
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Composable
fun BackupManagementScreen(
    onBackClick: () -> Unit,
    viewModel: BackupManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // No specific action needed, BackupState will update or user will retry
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackupManagementEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                BackupManagementEffect.RestartApp -> {
                    (context as? Activity)?.recreate()
                }
                is BackupManagementEffect.LaunchIntent -> {
                    authLauncher.launch(effect.intent)
                }
            }
        }
    }

    BackupManagementContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onAutoBackupToggled = viewModel::onAutoBackupToggled,
        onBackupConfirm = viewModel::startManualBackup
    )
}

@Composable
fun BackupManagementContent(
    uiState: BackupManagementUiState,
    onBackClick: () -> Unit,
    onAutoBackupToggled: (Boolean) -> Unit,
    onBackupConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ExpenseTrackerTheme.dimensions.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(CardBg)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Cloud Backup",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Last Backup Status", color = AccentPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Time: ${uiState.lastBackupTime}", color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("State: ${uiState.currentBackupState::class.simpleName}", color = TextSecondary, fontSize = 13.sp)
                }
            }

            // Automation Settings
            SectionTitle("Automation")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
            ) {
                Column {
                    SettingsSwitchRow("Auto Backup", "Enable daily cloud backups", uiState.isAutoBackupEnabled, onAutoBackupToggled)
                }
            }

            // Manual Controls
            SectionTitle("Actions")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
            ) {
                Column {
                    ActionRow("Backup Now", "Upload latest snapshot", Icons.Default.CloudUpload) {
                        onBackupConfirm()
                    }
                }
            }

            if (uiState.currentBackupState !is BackupState.Idle && uiState.currentBackupState !is BackupState.Completed && uiState.currentBackupState !is BackupState.Failed) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentPurple)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFFA855F7),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (enabled) TextPrimary else TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple))
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentPurple)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

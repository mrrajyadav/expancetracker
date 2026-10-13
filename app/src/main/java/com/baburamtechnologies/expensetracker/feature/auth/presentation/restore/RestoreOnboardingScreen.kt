package com.baburamtechnologies.expensetracker.feature.auth.presentation.restore

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baburamtechnologies.expensetracker.core.backup.domain.isActive
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun RestoreOnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: RestoreOnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.onPermissionResult()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RestoreOnboardingEffect.NavigateToHome -> onNavigateToHome()
                RestoreOnboardingEffect.RestartApp -> {
                    val activity = context as? Activity
                    val intent = activity?.packageManager?.getLaunchIntentForPackage(activity.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    activity?.startActivity(intent)
                    // Blunt but effective way to ensure all Singletons (like Database) are re-initialized
                    Runtime.getRuntime().exit(0)
                }
                is RestoreOnboardingEffect.RequestBackupPermission -> {
                    authLauncher.launch(effect.intent)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
            .padding(ExpenseTrackerTheme.dimensions.screenPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (uiState.noBackupFound) Icons.Default.CloudOff else Icons.Default.CloudDownload,
                contentDescription = null,
                tint = if (uiState.noBackupFound) TextSecondary else AccentPurple,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (uiState.noBackupFound) "No backup found" else "Welcome back",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when {
                    uiState.isLoading -> "Checking for your data on Google Drive..."
                    uiState.noBackupFound -> "We couldn't find any previous backup for this account. You can start fresh or try another account."
                    else -> "We found a backup of your data on Google Drive."
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (uiState.isLoading) {
                CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(48.dp))
            }

            uiState.backupMetadata?.let { metadata ->
                val formattedDate = remember(metadata.createdAt) {
                    try {
                        val instant = Instant.parse(metadata.createdAt)
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                            .withZone(ZoneId.systemDefault())
                        formatter.format(instant)
                    } catch (_: Exception) {
                        metadata.createdAt
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        InfoRow(label = "Date", value = formattedDate)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = "Version", value = metadata.appVersion)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            if (!uiState.isLoading) {
                if (uiState.backupMetadata != null) {
                    Button(
                        onClick = viewModel::onRestoreConfirm,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        enabled = !uiState.restoreState.isActive
                    ) {
                        if (uiState.restoreState.isActive) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Restore", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = viewModel::onContinueWithoutBackup,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !uiState.restoreState.isActive
                    ) {
                        Text(
                            text = "Continue without backup",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Button(
                        onClick = viewModel::onContinueWithoutBackup,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Start Fresh", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = viewModel::loadBackupMetadata,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            text = "Try searching again",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

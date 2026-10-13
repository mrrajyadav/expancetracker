package com.baburamtechnologies.expensetracker.feature.profile.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.profile.presentation.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(uiState.name) }
    var phone by remember { mutableStateOf(uiState.phone) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, focusedLabelColor = AccentPurple)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, focusedLabelColor = AccentPurple)
            )

            Button(
                onClick = { onSave(name, phone) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BackupRestoreDialog(
    isBackupMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBackupMode) "Backup Data" else "Restore Data", color = TextPrimary) },
        containerColor = CardBg,
        text = {
            Text(
                if (isBackupMode) "Are you sure you want to backup your data to Google Drive? This will overwrite any previous backup."
                else "Are you sure you want to restore your data from Google Drive? This will replace your current local data.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text(if (isBackupMode) "Backup Now" else "Restore Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account?", color = AccentRed) },
        containerColor = CardBg,
        text = {
            Text(
                "This action is permanent. All your data will be wiped from our servers and your local device. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Delete Permanently")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

package com.baburamtechnologies.expensetracker.feature.auth.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.AccentRed
import com.baburamtechnologies.expensetracker.core.designsystem.theme.CardBg
import com.baburamtechnologies.expensetracker.core.designsystem.theme.TextSecondary

@Composable
fun AuthErrorView(
    error: AuthUiError,
    onRetry: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = CardBg
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, title, message) = when (error) {
                AuthUiError.NoInternet -> Triple(Icons.Default.WifiOff, "No Internet", "Please check your network connection and try again.")
                AuthUiError.TooManyAttempts -> Triple(Icons.Default.ErrorOutline, "Too Many Attempts", "Please wait a few minutes before trying again.")
                else -> Triple(Icons.Default.ErrorOutline, "Error Occurred", "Something went wrong. Please try again.")
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            
            if (error == AuthUiError.NoInternet) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRetry) {
                    Text("Retry", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

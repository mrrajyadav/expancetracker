package com.baburamtechnologies.expensetracker.feature.auth.presentation.selection

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.LocalIndication
import com.baburamtechnologies.expensetracker.core.designsystem.component.AppLogoHeader

@Composable
fun AuthSelectionScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSuccess: (String) -> Unit,
    onNavigateToRestore: () -> Unit,
    viewModel: AuthSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.onPermissionResult()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AuthSelectionEffect.NavigateToHome -> onNavigateToHome()
                AuthSelectionEffect.NavigateToRestore -> onNavigateToRestore()
                is AuthSelectionEffect.RequestBackupPermission -> {
                    authLauncher.launch(effect.intent)
                }
                is AuthSelectionEffect.NavigateToSuccess -> onNavigateToSuccess(effect.message)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgStart, DarkBgEnd)))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Updated Logo using Component
            AppLogoHeader(
                modifier = Modifier.padding(top = 40.dp),
                iconSize = 110,
                titleSize = 32
            )

            // Welcome Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome 👋",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Log in with your Google account to get started",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            // Action Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthOptionCard(
                    text = "Continue with Google",
                    icon = null,
                    isGoogle = true,
                    isLoading = uiState.loadingAction == AuthSelectionAction.GoogleClick,
                    isInteractionDisabled = uiState.isLoading,
                    onClick = { viewModel.onAction(AuthSelectionAction.GoogleClick) }
                )
            }

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Private • Secure • Encrypted",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = buildAnnotatedString {
                        append("By continuing you agree to ")
                        withStyle(SpanStyle(color = AccentPurple, fontWeight = FontWeight.Bold)) { append("Terms") }
                        append(" & ")
                        withStyle(SpanStyle(color = AccentPurple, fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
                    },
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthOptionCard(
    text: String,
    icon: ImageVector?,
    isGoogle: Boolean = false,
    isLoading: Boolean = false,
    isInteractionDisabled: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = !isInteractionDisabled
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoogle) Color.White else CardBg,
            disabledContainerColor = if (isGoogle) Color.White else CardBg
        ),
        border = if (!isGoogle) androidx.compose.foundation.BorderStroke(1.dp, CardStroke) else null
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = if (isGoogle) Color(0xFFEA4335) else AccentPurple,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                if (isGoogle) {
                    Text(
                        text = "G",
                        color = Color(0xFFEA4335),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            Text(
                text = text,
                color = if (isGoogle) Color(0xFF1F2937) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

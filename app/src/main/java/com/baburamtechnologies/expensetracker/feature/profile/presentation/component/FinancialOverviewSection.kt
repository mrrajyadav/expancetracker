package com.baburamtechnologies.expensetracker.feature.profile.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.baburamtechnologies.expensetracker.R
import com.baburamtechnologies.expensetracker.core.designsystem.component.AppStatCard
import com.baburamtechnologies.expensetracker.core.designsystem.theme.AccentPurple
import com.baburamtechnologies.expensetracker.feature.profile.presentation.ProfileUiState

@Composable
fun FinancialOverviewSection(uiState: ProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.financial_overview),
            color = AccentPurple,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppStatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.total_income),
                value = "₹${uiState.totalIncome}",
                icon = Icons.Default.Savings,
                iconColor = Color(0xFF10B981)
            )
            AppStatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.total_expense),
                value = "₹${uiState.totalExpense}",
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = Color(0xFFEF4444)
            )
            AppStatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.savings),
                value = "₹${uiState.savings}",
                icon = Icons.Default.Diamond,
                iconColor = Color(0xFF8B5CF6)
            )
        }
    }
}

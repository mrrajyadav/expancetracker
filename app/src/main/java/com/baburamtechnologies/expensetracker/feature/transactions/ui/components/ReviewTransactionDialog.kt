package com.baburamtechnologies.expensetracker.feature.transactions.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.core.util.MoneyUtils
import com.baburamtechnologies.expensetracker.core.util.toFormattedCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewTransactionDialog(
    amount: String,
    type: String,
    categoryL1: String,
    categoryL2: String,
    categoryL3: String,
    paymentMode: String,
    note: String,
    dateMillis: Long,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedAmount = remember(amount) {
        MoneyUtils.parseAmountToPaise(amount)?.toFormattedCurrency() ?: "₹0.00"
    }

    val formattedDate = remember(dateMillis) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Review Transaction",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review transaction details",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBgStart),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow(label = "Amount", value = formattedAmount, icon = Icons.Default.Payments)
                        HorizontalDivider(color = CardStroke)
                        SummaryRow(label = "Type", value = type, icon = Icons.Default.Category)
                        HorizontalDivider(color = CardStroke)
                        SummaryRow(label = "Category L1", value = categoryL1, icon = Icons.Default.GridView)
                        HorizontalDivider(color = CardStroke)
                        SummaryRow(label = "Category L2", value = categoryL2, icon = Icons.Default.AccountTree)
                        if (categoryL3 != "None") {
                            HorizontalDivider(color = CardStroke)
                            SummaryRow(label = "Category L3", value = categoryL3, icon = Icons.AutoMirrored.Filled.List)
                        }
                        HorizontalDivider(color = CardStroke)
                        SummaryRow(label = "Payment", value = paymentMode, icon = Icons.Default.AccountBalanceWallet)
                        if (note.isNotBlank()) {
                            HorizontalDivider(color = CardStroke)
                            SummaryRow(label = "Note", value = note, icon = Icons.Default.EditNote)
                        }
                        HorizontalDivider(color = CardStroke)
                        SummaryRow(label = "Date", value = formattedDate, icon = Icons.Default.CalendarToday)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple)
                    ) {
                        Text("Edit", color = AccentPurple)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}

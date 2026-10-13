package com.baburamtechnologies.expensetracker.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*
import com.baburamtechnologies.expensetracker.feature.profile.presentation.ProfileUiState

import androidx.compose.ui.res.stringResource
import com.baburamtechnologies.expensetracker.R

@Composable
fun ProfileHeader(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Area
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.1f))
                        .clickable { onEditProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profileImageUrl != null) {
                        AsyncImage(
                            model = uiState.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(36.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2746))
                            .border(1.dp, CardBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditProfileClick() }
                ) {
                    Text(
                        text = uiState.name.ifBlank { "User" },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = uiState.email,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    
                    if (uiState.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.phone,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                
                IconButton(onClick = onEditProfileClick) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Edit Profile", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = CardStroke, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatItem(stringResource(R.string.balance), "₹${uiState.totalBalance}", modifier = Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(24.dp), color = CardStroke)
                ProfileStatItem("Transactions", "${uiState.transactionCount}", modifier = Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(24.dp), color = CardStroke)
                ProfileStatItem(stringResource(R.string.member_since), uiState.memberSince, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

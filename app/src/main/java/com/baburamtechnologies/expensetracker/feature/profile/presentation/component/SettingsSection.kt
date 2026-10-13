package com.baburamtechnologies.expensetracker.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.*

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = AccentPurple,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardStroke),
        content = content
    )
}

@Composable
fun SettingsOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDangerous: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (isDangerous) AccentRed.copy(alpha = 0.1f) else AccentPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isDangerous) AccentRed else AccentPurple, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (isDangerous) AccentRed else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (enabled) TextPrimary else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, colors = SwitchDefaults.colors(checkedThumbColor = AccentPurple, checkedTrackColor = AccentPurple.copy(alpha = 0.5f)))
    }
}

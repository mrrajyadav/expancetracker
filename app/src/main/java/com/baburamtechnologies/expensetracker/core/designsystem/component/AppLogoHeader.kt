package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.R
import com.baburamtechnologies.expensetracker.core.designsystem.theme.AccentPurple
import com.baburamtechnologies.expensetracker.core.designsystem.theme.TextSecondary

@Composable
fun AppLogoHeader(
    modifier: Modifier = Modifier,
    iconSize: Int = 100,
    titleSize: Int = 28,
    showSlogan: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = titleSize.sp)) { append("Expense ") }
                withStyle(SpanStyle(color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = titleSize.sp)) { append("Tracker") }
            }
        )
        
        if (showSlogan) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = AccentPurple.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentPurple))
                    }
                }
                HorizontalDivider(modifier = Modifier.weight(1f), color = AccentPurple.copy(alpha = 0.3f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Track  •  Manage  •  Save",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

package com.baburamtechnologies.expensetracker.feature.auth.presentation.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baburamtechnologies.expensetracker.core.designsystem.theme.AccentPurple
import com.baburamtechnologies.expensetracker.core.designsystem.theme.CardBg
import com.baburamtechnologies.expensetracker.core.designsystem.theme.CardStroke
import com.baburamtechnologies.expensetracker.core.designsystem.theme.TextSecondary

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    trailingIconTint: Color = TextSecondary,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = AccentPurple) },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                } else if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = trailingIconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = CardStroke,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() })
        )
    }
}

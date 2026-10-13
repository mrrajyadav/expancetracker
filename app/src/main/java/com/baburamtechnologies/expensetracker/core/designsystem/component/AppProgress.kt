package com.baburamtechnologies.expensetracker.core.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp

@Composable
fun AppCircularProgress(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

@Composable
fun AppLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap
    )
}

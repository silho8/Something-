package com.eclipse.launcher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eclipse.launcher.presentation.viewmodel.ClockViewModel
import com.eclipse.launcher.ui.theme.GlassCard
import com.eclipse.launcher.ui.theme.LocalDynamicThemeColors

@Composable
fun ClockWidget(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = hiltViewModel()
) {
    val timeString by viewModel.timeString.collectAsState()
    val dateString by viewModel.dateString.collectAsState()

    val dynamicThemeColors = LocalDynamicThemeColors.current

    // Adaptive colors based on extracted wallpaper themes
    val widgetTint = dynamicThemeColors.widgetColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    val textTint = dynamicThemeColors.iconTintColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = widgetTint,
        transparency = 0.4f,
        blurIntensity = 25.dp,
        cornerRadius = 32.dp,
        contentPadding = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timeString,
                // Using displayLarge because the dynamic typography engine overrides it with NDot
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = textTint
            )

            Text(
                text = dateString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                ),
                color = textTint.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

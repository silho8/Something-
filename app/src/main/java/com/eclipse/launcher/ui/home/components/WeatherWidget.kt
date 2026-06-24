package com.eclipse.launcher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eclipse.launcher.ui.theme.GlassCard
import com.eclipse.launcher.ui.theme.LocalDynamicThemeColors

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    val dynamicThemeColors = LocalDynamicThemeColors.current
    val widgetTint = dynamicThemeColors.widgetColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    val textTint = dynamicThemeColors.iconTintColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurface

    GlassCard(
        modifier = modifier.fillMaxSize(),
        backgroundColor = widgetTint,
        transparency = 0.4f,
        blurIntensity = 25.dp,
        cornerRadius = 32.dp,
        contentPadding = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "72°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = textTint
            )
            Text(
                text = "Mostly Sunny",
                style = MaterialTheme.typography.bodyLarge,
                color = textTint.copy(alpha = 0.8f)
            )
        }
    }
}

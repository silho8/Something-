package com.eclipse.launcher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.eclipse.launcher.domain.model.DynamicThemeColors
import com.eclipse.launcher.domain.TypographyStyle

@Composable
fun EclipseLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    typographyStyle: TypographyStyle = TypographyStyle.NDOT_WIDGETS_ONLY,
    dynamicThemeColors: DynamicThemeColors? = null,
    content: @Composable () -> Unit
) {
    val extractedPrimary = dynamicThemeColors?.dominantColor?.let { Color(it) } ?: Purple40
    val extractedSecondary = dynamicThemeColors?.secondaryColor?.let { Color(it) } ?: PurpleGrey40
    val extractedTertiary = dynamicThemeColors?.accentColor?.let { Color(it) } ?: Pink40
    val extractedBackground = dynamicThemeColors?.backgroundColor?.let { Color(it) } ?: Color.Black
    val extractedSurface = dynamicThemeColors?.widgetColor?.let { Color(it) } ?: Color.DarkGray

    val colorScheme = darkColorScheme(
        primary = extractedPrimary,
        secondary = extractedSecondary,
        tertiary = extractedTertiary,
        background = extractedBackground,
        surface = extractedSurface,
        onSurface = dynamicThemeColors?.iconTintColor?.let { Color(it) } ?: Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val dynamicTypography = getTypography(typographyStyle)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}

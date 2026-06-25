package com.eclipse.launcher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eclipse.launcher.domain.model.TypographyStyle

// Define default text styles based on Material 3 guidelines
private val defaultDisplayLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp
)
private val defaultDisplayMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp
)
private val defaultDisplaySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp
)

private val defaultHeadlineLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
)
private val defaultHeadlineMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
)
private val defaultHeadlineSmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

private val defaultBodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)
private val defaultBodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)
private val defaultBodySmall = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
)

fun getTypography(style: TypographyStyle): Typography {
    val displayFontFamily = when (style) {
        TypographyStyle.NDOT_EVERYWHERE, TypographyStyle.NDOT_WIDGETS_ONLY -> FontManager.NDotFontFamily
        TypographyStyle.SYSTEM_FONT -> FontFamily.Default
    }

    val bodyFontFamily = when (style) {
        TypographyStyle.NDOT_EVERYWHERE -> FontManager.NDotFontFamily
        TypographyStyle.NDOT_WIDGETS_ONLY, TypographyStyle.SYSTEM_FONT -> FontFamily.Default
    }

    return Typography(
        displayLarge = defaultDisplayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = defaultDisplayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = defaultDisplaySmall.copy(fontFamily = displayFontFamily),

        headlineLarge = defaultHeadlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = defaultHeadlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = defaultHeadlineSmall.copy(fontFamily = displayFontFamily),

        bodyLarge = defaultBodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = defaultBodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = defaultBodySmall.copy(fontFamily = bodyFontFamily)
    )
}

// Keeping a fallback static Typography object if needed, using the default style.
val Typography = getTypography(TypographyStyle.NDOT_WIDGETS_ONLY)

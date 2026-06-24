package com.eclipse.launcher.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.eclipse.launcher.R

object FontManager {
    val NDotFontFamily = FontFamily(
        Font(R.font.ndot, FontWeight.Normal)
        // If other weights are added, they can be listed here, e.g., Font(R.font.ndot_bold, FontWeight.Bold)
    )
}

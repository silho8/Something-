package com.eclipse.launcher.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.DynamicThemeColors
import com.eclipse.launcher.domain.repository.DynamicThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DynamicThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val homePreferences: HomePreferences
) : DynamicThemeRepository {

    override fun getDynamicThemeColors(): Flow<DynamicThemeColors> {
        return homePreferences.getWallpaperPath().map { path ->
            if (path.isNullOrEmpty()) {
                DynamicThemeColors() // Returns default (all nulls)
            } else {
                extractColorsFromPath(path)
            }
        }
    }

    private suspend fun extractColorsFromPath(path: String): DynamicThemeColors = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) {
            return@withContext DynamicThemeColors()
        }

        // Downsample bitmap for faster palette generation
        val options = BitmapFactory.Options().apply {
            inSampleSize = 4
        }
        val bitmap = BitmapFactory.decodeFile(path, options) ?: return@withContext DynamicThemeColors()

        val palette = Palette.from(bitmap).generate()

        val dominantColor = palette.getDominantColor(Color.BLACK)
        val secondaryColor = palette.getMutedColor(dominantColor)
        val accentColor = palette.getVibrantColor(dominantColor)

        // Calculate derivatives
        val backgroundColor = adjustAlpha(dominantColor, 0.9f) // Slight transparency for the wallpaper
        val widgetColor = adjustAlpha(secondaryColor, 0.8f) // Frosted glass effect base
        val dockColor = adjustAlpha(dominantColor, 0.7f) // Slightly darker than background

        // Ensure contrast for icons
        val isDarkBackground = ColorUtils.calculateLuminance(dominantColor) < 0.5
        val iconTintColor = if (isDarkBackground) {
            Color.WHITE
        } else {
            Color.BLACK
        }

        DynamicThemeColors(
            dominantColor = dominantColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor,
            backgroundColor = backgroundColor,
            widgetColor = widgetColor,
            dockColor = dockColor,
            iconTintColor = iconTintColor
        )
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(alpha, r, g, b)
    }
}

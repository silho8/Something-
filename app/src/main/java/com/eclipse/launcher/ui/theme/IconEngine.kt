package com.eclipse.launcher.ui.theme

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.eclipse.launcher.domain.model.IconStyle

@Composable
fun StyledIcon(
    drawable: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val iconStyle = LocalIconStyle.current
    val dynamicThemeColors = LocalDynamicThemeColors.current

    if (drawable == null) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        )
        return
    }

    when (iconStyle) {
        IconStyle.ORIGINAL -> {
            Image(
                bitmap = drawable.toBitmap().asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        IconStyle.MONOCHROME -> {
            // Apply a monochrome color matrix filter to the original icon
            val matrix = ColorMatrix().apply {
                setToSaturation(0f)
            }
            Image(
                bitmap = drawable.toBitmap().asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier.clip(CircleShape),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(matrix)
            )
        }
        IconStyle.GLASS -> {
            // Extract adaptive foreground if possible, otherwise use whole icon
            val fgDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
                drawable.foreground
            } else {
                drawable
            }

            val tintColor = dynamicThemeColors.iconTintColor?.let { Color(it) } ?: Color.White
            val dockColor = dynamicThemeColors.dockColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface

            GlassContainer(
                modifier = modifier,
                shape = CircleShape,
                backgroundColor = dockColor,
                transparency = 0.5f,
                blurIntensity = 25.dp
            ) {
                Image(
                    bitmap = fgDrawable.toBitmap().asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(tintColor)
                )
            }
        }
        IconStyle.GLASS_DARK -> {
            val fgDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
                drawable.foreground
            } else {
                drawable
            }

            GlassContainer(
                modifier = modifier,
                shape = CircleShape,
                backgroundColor = Color.Black,
                transparency = 0.7f,
                blurIntensity = 25.dp
            ) {
                Image(
                    bitmap = fgDrawable.toBitmap().asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}

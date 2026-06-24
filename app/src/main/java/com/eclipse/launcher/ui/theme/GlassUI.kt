package com.eclipse.launcher.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a basic glass-like appearance using background transparency and a subtle border.
 */
fun Modifier.glassAppearance(
    backgroundColor: Color,
    transparency: Float,
    shape: Shape,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.2f)
): Modifier = this
    .clip(shape)
    .background(backgroundColor.copy(alpha = transparency))
    .border(borderWidth, borderColor, shape)

/**
 * A reusable Glass Background composable.
 * Useful for large areas like the App Drawer background.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    transparency: Float = 0.4f,
    blurIntensity: Dp = 30.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // The blurring layer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = blurIntensity, edgeTreatment = BlurredEdgeTreatment.Rectangle)
            )
        }

        // The frosted tint layer and content
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor.copy(alpha = transparency))
        ) {
            content()
        }
    }
}

/**
 * A reusable Glass Container.
 * Useful for circular or custom-shaped frosted elements like Folders.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    transparency: Float = 0.4f,
    blurIntensity: Dp = 15.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = blurIntensity, edgeTreatment = BlurredEdgeTreatment.Rectangle)
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor.copy(alpha = transparency))
        )

        content()
    }
}

/**
 * A reusable Glass Card.
 * Useful for widgets or padded containers.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    transparency: Float = 0.4f,
    blurIntensity: Dp = 20.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        backgroundColor = backgroundColor,
        transparency = transparency,
        blurIntensity = blurIntensity
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

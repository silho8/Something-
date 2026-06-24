package com.eclipse.launcher.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.ui.home.LocalDragDropState
import com.eclipse.launcher.ui.theme.GlassContainer
import com.eclipse.launcher.ui.theme.LocalDynamicThemeColors

@Composable
fun FloatingDock(
    items: List<LauncherItem>,
    onItemDropped: (item: LauncherItem, position: GridPosition) -> Unit,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicThemeColors = LocalDynamicThemeColors.current
    val dragDropState = LocalDragDropState.current

    // Adaptive tint from wallpaper or fallback
    val dockTint = dynamicThemeColors.dockColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surface

    GlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        backgroundColor = dockTint,
        transparency = 0.5f,
        blurIntensity = 25.dp
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val usableWidth = screenWidth - 32.dp // subtract padding
        val cellWidth = usableWidth / 5

        Box(modifier = Modifier.fillMaxSize()) {
            // Draw static dock items
            items.filter { it.id != dragDropState.draggedItem?.id }.forEach { item ->
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .fillMaxHeight()
                        .offset(x = cellWidth * item.position.column)
                ) {
                    LauncherItemView(
                        item = item,
                        isDragging = false,
                        onAppClick = onAppClick
                    )
                }
            }

            // Draw dragged dock item overlay
            dragDropState.draggedItem?.let { dragged ->
                if (dragged.position.row == -2 || dragDropState.dropPosition?.row == -2) {
                    val originalX = (dragged.position.column * cellWidth.value).dp
                    val currentX = originalX + with(androidx.compose.ui.platform.LocalDensity.current) { dragDropState.dragPosition.x.toDp() }

                    if (dragDropState.isDropped) {
                        LaunchedEffect(dragDropState.isDropped) {
                            val dropCol = ((currentX.value + (cellWidth.value / 2)) / cellWidth.value).toInt()

                            if (dropCol in 0..4) {
                                onItemDropped(dragged, GridPosition(0, -2, dropCol))
                            } else {
                                onItemDropped(dragged, dragged.position)
                            }
                            dragDropState.onDropConsumed()
                        }
                    } else if (dragDropState.isDragging) {
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .fillMaxHeight()
                                .offset(x = currentX)
                        ) {
                            LauncherItemView(
                                item = dragged,
                                isDragging = true,
                                onAppClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
    }
}

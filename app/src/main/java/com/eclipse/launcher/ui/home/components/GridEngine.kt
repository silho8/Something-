package com.eclipse.launcher.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.ui.home.LocalDragDropState
import kotlin.math.roundToInt

@Composable
fun GridEngine(
    page: Int,
    items: List<LauncherItem>,
    columns: Int = 5,
    rows: Int = 6,
    onItemDropped: (item: LauncherItem, position: GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val dragDropState = LocalDragDropState.current
    var gridSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                gridSize = coordinates.size
            }
    ) {
        if (gridSize.width > 0 && gridSize.height > 0) {
            val cellWidth = gridSize.width / columns
            val cellHeight = gridSize.height / rows

            // Draw normal items
            items.filter { it.id != dragDropState.draggedItem?.id }.forEach { item ->
                val xOffset = item.position.column * cellWidth
                val yOffset = item.position.row * cellHeight

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xOffset, yOffset) }
                        .size(
                            width = with(androidx.compose.ui.platform.LocalDensity.current) { cellWidth.toDp() },
                            height = with(androidx.compose.ui.platform.LocalDensity.current) { cellHeight.toDp() }
                        )
                ) {
                    LauncherItemView(item = item, isDragging = false)
                }
            }

            // Draw dragged item overlay
            dragDropState.draggedItem?.let { dragged ->
                if (dragged.position.page == page) {
                    val originalX = dragged.position.column * cellWidth
                    val originalY = dragged.position.row * cellHeight

                    val currentX = (originalX + dragDropState.dragPosition.x).roundToInt()
                    val currentY = (originalY + dragDropState.dragPosition.y).roundToInt()

                    if (dragDropState.isDropped) {
                        LaunchedEffect(dragDropState.isDropped) {
                            val dropCol = (currentX + cellWidth / 2) / cellWidth
                            val dropRow = (currentY + cellHeight / 2) / cellHeight

                            if (dropCol in 0 until columns && dropRow in 0 until rows) {
                                onItemDropped(dragged, GridPosition(page, dropRow, dropCol))
                            } else {
                                onItemDropped(dragged, dragged.position)
                            }
                            dragDropState.onDropConsumed()
                        }
                    } else if (dragDropState.isDragging) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(currentX, currentY) }
                                .size(
                                    width = with(androidx.compose.ui.platform.LocalDensity.current) { cellWidth.toDp() },
                                    height = with(androidx.compose.ui.platform.LocalDensity.current) { cellHeight.toDp() }
                                )
                        ) {
                            LauncherItemView(item = dragged, isDragging = true)
                        }
                    }
                }
            }
        }
    }
}

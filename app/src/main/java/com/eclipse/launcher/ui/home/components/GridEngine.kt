package com.eclipse.launcher.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.eclipse.launcher.domain.model.WidgetSize
import com.eclipse.launcher.domain.registry.WidgetRegistry
import com.eclipse.launcher.ui.home.LocalDragDropState
import kotlin.math.roundToInt

@Composable
fun GridEngine(
    page: Int,
    items: List<LauncherItem>,
    columns: Int = 5,
    rows: Int = 6,
    widgetRegistry: WidgetRegistry,
    onItemDropped: (item: LauncherItem, position: GridPosition) -> Unit,
    onAppClick: (String) -> Unit,
    onWidgetResize: (LauncherItem.WidgetItem, deltaXCols: Int, deltaYRows: Int) -> Unit,
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
            val cellWidth = remember(gridSize.width, columns) { gridSize.width / columns }
            val cellHeight = remember(gridSize.height, rows) { gridSize.height / rows }

            // Draw normal items
            val staticItems by remember(items, dragDropState.draggedItem) {
                derivedStateOf { items.filter { it.id != dragDropState.draggedItem?.id } }
            }

            staticItems.forEach { item ->
                val xOffset = remember(item.position.column, cellWidth) { item.position.column * cellWidth }
                val yOffset = remember(item.position.row, cellHeight) { item.position.row * cellHeight }
                val itemSize = if (item is LauncherItem.WidgetItem) item.size else WidgetSize(1, 1)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xOffset, yOffset) }
                        .size(
                            width = with(androidx.compose.ui.platform.LocalDensity.current) { (cellWidth * itemSize.spanColumns).toDp() },
                            height = with(androidx.compose.ui.platform.LocalDensity.current) { (cellHeight * itemSize.spanRows).toDp() }
                        )
                ) {
                    LauncherItemView(
                        item = item,
                        widgetRegistry = widgetRegistry,
                        isDragging = false,
                        onAppClick = onAppClick,
                        onWidgetResize = { widget, deltaX, deltaY ->
                            val dxCols = (deltaX / cellWidth).roundToInt()
                            val dyRows = (deltaY / cellHeight).roundToInt()
                            if (dxCols != 0 || dyRows != 0) {
                                onWidgetResize(widget, dxCols, dyRows)
                            }
                        }
                    )
                }
            }

            // Draw dragged item overlay
            dragDropState.draggedItem?.let { dragged ->
                if (dragged.position.page == page || dragDropState.dropPosition?.page == page) {
                    val originalX = dragged.position.column * cellWidth
                    val originalY = dragged.position.row * cellHeight

                    val currentX = (originalX + dragDropState.dragPosition.x).roundToInt()
                    val currentY = (originalY + dragDropState.dragPosition.y).roundToInt()

                    val draggedSize = if (dragged is LauncherItem.WidgetItem) dragged.size else WidgetSize(1, 1)

                    if (dragDropState.isDropped) {
                        LaunchedEffect(dragDropState.isDropped) {
                            val dropCol = (currentX + cellWidth / 2) / cellWidth
                            val dropRow = (currentY + cellHeight / 2) / cellHeight

                            if (dropCol >= 0 && dropCol + draggedSize.spanColumns <= columns &&
                                dropRow >= 0 && dropRow + draggedSize.spanRows <= rows) {
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
                                    width = with(androidx.compose.ui.platform.LocalDensity.current) { (cellWidth * draggedSize.spanColumns).toDp() },
                                    height = with(androidx.compose.ui.platform.LocalDensity.current) { (cellHeight * draggedSize.spanRows).toDp() }
                                )
                        ) {
                            LauncherItemView(
                                item = dragged,
                                widgetRegistry = widgetRegistry,
                                isDragging = true,
                                onAppClick = onAppClick,
                                onWidgetResize = {_,_,_->} // Disable resize while dragging
                            )
                        }
                    }
                }
            }
        }
    }
}

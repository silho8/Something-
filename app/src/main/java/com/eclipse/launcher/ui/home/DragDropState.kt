package com.eclipse.launcher.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem

class DragDropState {
    var isDragging by mutableStateOf(false)
        private set
    var draggedItem by mutableStateOf<LauncherItem?>(null)
        private set
    var dragPosition by mutableStateOf(Offset.Zero)
        private set

    var dropPosition by mutableStateOf<GridPosition?>(null)
        internal set

    var isDropped by mutableStateOf(false)
        private set

    fun onDragStart(item: LauncherItem) {
        isDragging = true
        isDropped = false
        draggedItem = item
        dragPosition = Offset.Zero // Start smoothly from 0
    }

    fun onDrag(offset: Offset) {
        dragPosition += offset
    }

    fun onDragEnd() {
        isDragging = false
        isDropped = true
    }

    fun onDropConsumed() {
        isDropped = false
        draggedItem = null
        dragPosition = Offset.Zero
        dropPosition = null
    }
}

val LocalDragDropState = compositionLocalOf<DragDropState> { error("No DragDropState provided") }

@Composable
fun DragDropProvider(content: @Composable () -> Unit) {
    val state = remember { DragDropState() }
    CompositionLocalProvider(LocalDragDropState provides state) {
        content()
    }
}

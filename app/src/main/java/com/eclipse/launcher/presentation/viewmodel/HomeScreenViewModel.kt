package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.AppSortType
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.domain.model.WidgetSize
import com.eclipse.launcher.domain.model.WidgetType
import com.eclipse.launcher.domain.registry.WidgetRegistry
import com.eclipse.launcher.domain.repository.InstalledAppsManager
import com.eclipse.launcher.ui.home.HomeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val installedAppsManager: InstalledAppsManager,
    private val homePreferences: HomePreferences,
    val widgetRegistry: WidgetRegistry // Expose for UI
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val columns = 5
    private val rows = 6
    private val itemsPerPage = columns * rows

    init {
        loadHomeData()
        observeWallpaper()
    }

    private fun observeWallpaper() {
        viewModelScope.launch {
            homePreferences.getWallpaperPath().collect { path ->
                _state.update { it.copy(wallpaperPath = path) }
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val installedApps = installedAppsManager.getInstalledApps(AppSortType.ALPHABETICAL_ASC)
            val savedState = homePreferences.getSavedGridState().first()
            val savedDockState = homePreferences.getSavedDockState().first()

            val finalItems = mutableListOf<LauncherItem>()
            val finalDockItems = mutableListOf<LauncherItem>()

            // Load Dock First
            if (savedDockState.isEmpty()) {
                val defaultDockApps = installedApps.take(5)
                defaultDockApps.forEachIndexed { index, app ->
                    finalDockItems.add(app.copy(position = GridPosition(page = 0, row = -2, column = index)))
                }
            } else {
                savedDockState.forEach { savedItem ->
                    if (savedItem.isFolder) {
                        val folderApps = savedItem.folderContents?.mapNotNull { id ->
                            installedApps.find { it.id == id }
                        } ?: emptyList()

                        if (folderApps.isNotEmpty()) {
                            finalDockItems.add(
                                LauncherItem.FolderItem(
                                    id = savedItem.id,
                                    name = savedItem.folderName ?: "Folder",
                                    apps = folderApps,
                                    position = GridPosition(savedItem.page, savedItem.row, savedItem.column)
                                )
                            )
                        }
                    } else if (savedItem.isWidget && savedItem.widgetType != null) {
                        // Dock widgets usually not supported, but parsed just in case
                        finalDockItems.add(
                            LauncherItem.WidgetItem(
                                id = savedItem.id,
                                widgetType = WidgetType.valueOf(savedItem.widgetType),
                                size = WidgetSize(savedItem.spanCols ?: 1, savedItem.spanRows ?: 1),
                                position = GridPosition(savedItem.page, savedItem.row, savedItem.column)
                            )
                        )
                    } else {
                        installedApps.find { it.id == savedItem.id }?.let {
                            finalDockItems.add(it.copy(position = GridPosition(savedItem.page, savedItem.row, savedItem.column)))
                        }
                    }
                }
            }

            // Load Grid
            val dockIds = finalDockItems.flatMap {
                when (it) {
                    is LauncherItem.FolderItem -> it.apps.map { a -> a.id }
                    else -> listOf(it.id)
                }
            }.toSet()

            if (savedState.isEmpty()) {
                // Initial default state with widgets
                val clockWidget = LauncherItem.WidgetItem(
                    id = UUID.randomUUID().toString(),
                    widgetType = WidgetType.CLOCK,
                    size = widgetRegistry.getDefaultSize(WidgetType.CLOCK),
                    position = GridPosition(page = 0, row = 0, column = 0)
                )
                finalItems.add(clockWidget)

                // Auto-place apps not in dock
                val gridApps = installedApps.filter { it.id !in dockIds }
                var nextEmpty = findNextEmptyCell(finalItems, WidgetSize(1,1))
                gridApps.forEach { app ->
                    finalItems.add(app.copy(position = nextEmpty))
                    nextEmpty = findNextEmptyCell(finalItems, WidgetSize(1,1))
                }
            } else {
                savedState.forEach { savedItem ->
                    if (savedItem.isFolder) {
                        val folderApps = savedItem.folderContents?.mapNotNull { id ->
                            installedApps.find { it.id == id }
                        } ?: emptyList()

                        if (folderApps.isNotEmpty()) {
                            finalItems.add(
                                LauncherItem.FolderItem(
                                    id = savedItem.id,
                                    name = savedItem.folderName ?: "Folder",
                                    apps = folderApps,
                                    position = GridPosition(savedItem.page, savedItem.row, savedItem.column)
                                )
                            )
                        }
                    } else if (savedItem.isWidget && savedItem.widgetType != null) {
                        finalItems.add(
                            LauncherItem.WidgetItem(
                                id = savedItem.id,
                                widgetType = WidgetType.valueOf(savedItem.widgetType),
                                size = WidgetSize(savedItem.spanCols ?: 1, savedItem.spanRows ?: 1),
                                position = GridPosition(savedItem.page, savedItem.row, savedItem.column)
                            )
                        )
                    } else {
                        installedApps.find { it.id == savedItem.id }?.let {
                            finalItems.add(it.copy(position = GridPosition(savedItem.page, savedItem.row, savedItem.column)))
                        }
                    }
                }

                val savedAppIds = savedState.flatMap {
                    when {
                        it.isFolder -> it.folderContents ?: emptyList()
                        it.isWidget -> emptyList()
                        else -> listOf(it.id)
                    }
                }.toSet()

                var nextEmpty = findNextEmptyCell(finalItems, WidgetSize(1,1))
                installedApps.filter { it.id !in savedAppIds && it.id !in dockIds }.forEach { newApp ->
                    finalItems.add(newApp.copy(position = nextEmpty))
                    nextEmpty = findNextEmptyCell(finalItems + newApp.copy(position = nextEmpty), WidgetSize(1,1))
                }
            }

            val maxPage = finalItems.maxOfOrNull { it.position.page } ?: 0
            val pagesMap = finalItems.groupBy { it.position.page }

            _state.update {
                it.copy(
                    isLoading = false,
                    pages = pagesMap,
                    totalPages = maxPage + 1,
                    dockItems = finalDockItems
                )
            }
        }
    }

    private fun findNextEmptyCell(currentItems: List<LauncherItem>, size: WidgetSize): GridPosition {
        val maxPage = currentItems.maxOfOrNull { it.position.page } ?: 0
        for (page in 0..maxPage + 1) {
            for (row in 0..rows - size.spanRows) {
                for (col in 0..columns - size.spanColumns) {
                    val potentialPos = GridPosition(page, row, col)
                    if (!checkCollision(potentialPos, size, currentItems, null)) {
                        return potentialPos
                    }
                }
            }
        }
        return GridPosition(maxPage + 1, 0, 0)
    }

    // Check if placing an item of `size` at `pos` collides with any item in `currentItems` (excluding `ignoreItemId`)
    private fun checkCollision(
        pos: GridPosition,
        size: WidgetSize,
        currentItems: List<LauncherItem>,
        ignoreItemId: String?
    ): Boolean {
        val targetCols = pos.column until (pos.column + size.spanColumns)
        val targetRows = pos.row until (pos.row + size.spanRows)

        return currentItems.any { item ->
            if (item.id == ignoreItemId) return@any false
            if (item.position.page != pos.page) return@any false

            val itemSize = if (item is LauncherItem.WidgetItem) item.size else WidgetSize(1, 1)
            val itemCols = item.position.column until (item.position.column + itemSize.spanColumns)
            val itemRows = item.position.row until (item.position.row + itemSize.spanRows)

            // Collision if row and column ranges intersect
            (targetCols.intersect(itemCols).isNotEmpty() && targetRows.intersect(itemRows).isNotEmpty())
        }
    }

    fun onItemMoved(item: LauncherItem, newPosition: GridPosition) {
        val currentGridItems = _state.value.pages.values.flatten().toMutableList()
        val currentDockItems = _state.value.dockItems.toMutableList()

        currentGridItems.removeIf { it.id == item.id }
        currentDockItems.removeIf { it.id == item.id }

        val isTargetingDock = newPosition.row == -2
        val targetList = if (isTargetingDock) currentDockItems else currentGridItems
        val itemSize = if (item is LauncherItem.WidgetItem) item.size else WidgetSize(1, 1)

        // Ensure bounds
        if (!isTargetingDock) {
            if (newPosition.column + itemSize.spanColumns > columns || newPosition.row + itemSize.spanRows > rows) {
                // Out of bounds, reject
                if (item.position.row == -2) currentDockItems.add(item) else currentGridItems.add(item)
                saveAndEmitState(currentGridItems, currentDockItems)
                return
            }
        }

        if (isTargetingDock) {
            if (item is LauncherItem.WidgetItem) {
                // Widgets can't go in dock
                currentGridItems.add(item)
                saveAndEmitState(currentGridItems, currentDockItems)
                return
            }

            val targetItem = targetList.find { it.position == newPosition }
            if (targetItem != null && targetItem.id != item.id) {
                targetList.removeIf { it.id == targetItem.id }
                mergeItems(item, targetItem, newPosition, targetList)
            } else if (currentDockItems.size >= 5 && targetItem == null) {
                currentGridItems.add(item) // Bounce back
            } else {
                targetList.add(updateItemPosition(item, newPosition))
            }
        } else {
            // Targeting Grid: check collision with variable sizes
            val collidedItems = targetList.filter { target ->
                if (target.id == item.id) return@filter false
                if (target.position.page != newPosition.page) return@filter false

                val targetSize = if (target is LauncherItem.WidgetItem) target.size else WidgetSize(1, 1)
                val targetCols = target.position.column until (target.position.column + targetSize.spanColumns)
                val targetRows = target.position.row until (target.position.row + targetSize.spanRows)

                val sourceCols = newPosition.column until (newPosition.column + itemSize.spanColumns)
                val sourceRows = newPosition.row until (newPosition.row + itemSize.spanRows)

                sourceCols.intersect(targetCols).isNotEmpty() && sourceRows.intersect(targetRows).isNotEmpty()
            }

            if (collidedItems.size == 1 && itemSize.spanColumns == 1 && itemSize.spanRows == 1) {
                // Single 1x1 item collision -> attempt folder merge
                val targetItem = collidedItems.first()
                if (targetItem !is LauncherItem.WidgetItem) {
                    targetList.removeIf { it.id == targetItem.id }
                    mergeItems(item, targetItem, newPosition, targetList)
                    saveAndEmitState(currentGridItems, currentDockItems)
                    return
                }
            }

            if (collidedItems.isNotEmpty()) {
                // Invalid overlap (e.g. dropping widget on apps, dropping app on widget) -> Reject
                if (item.position.row == -2) currentDockItems.add(item) else currentGridItems.add(item)
            } else {
                targetList.add(updateItemPosition(item, newPosition))
            }
        }

        saveAndEmitState(currentGridItems, currentDockItems)
    }

    private fun mergeItems(source: LauncherItem, target: LauncherItem, pos: GridPosition, list: MutableList<LauncherItem>) {
        if (target is LauncherItem.FolderItem && source is LauncherItem.AppItem) {
            val newFolder = target.copy(apps = target.apps + source, position = pos)
            list.add(newFolder)
        } else if (target is LauncherItem.AppItem && source is LauncherItem.AppItem) {
            val newFolder = LauncherItem.FolderItem(
                id = UUID.randomUUID().toString(),
                name = "Folder",
                apps = listOf(target, source),
                position = pos
            )
            list.add(newFolder)
        } else {
            // Cannot merge
            list.add(source)
            list.add(target)
        }
    }

    private fun updateItemPosition(item: LauncherItem, pos: GridPosition): LauncherItem {
        return when (item) {
            is LauncherItem.AppItem -> item.copy(position = pos)
            is LauncherItem.FolderItem -> item.copy(position = pos)
            is LauncherItem.WidgetItem -> item.copy(position = pos)
        }
    }

    fun onWidgetResize(item: LauncherItem.WidgetItem, deltaXCols: Int, deltaYRows: Int) {
        val currentGridItems = _state.value.pages.values.flatten().toMutableList()
        val currentDockItems = _state.value.dockItems.toList()

        currentGridItems.removeIf { it.id == item.id }

        val minSize = widgetRegistry.getMinSize(item.widgetType)
        val maxSize = widgetRegistry.getMaxSize(item.widgetType)

        val newCols = (item.size.spanColumns + deltaXCols).coerceIn(minSize.spanColumns, maxSize.spanColumns)
        val newRows = (item.size.spanRows + deltaYRows).coerceIn(minSize.spanRows, maxSize.spanRows)

        val newSize = WidgetSize(newCols, newRows)

        // Check bounds
        if (item.position.column + newSize.spanColumns > columns || item.position.row + newSize.spanRows > rows) {
            currentGridItems.add(item) // Revert
            saveAndEmitState(currentGridItems, currentDockItems)
            return
        }

        // Check collisions for the new size
        if (checkCollision(item.position, newSize, currentGridItems, null)) {
             currentGridItems.add(item) // Revert
        } else {
             currentGridItems.add(item.copy(size = newSize))
        }

        saveAndEmitState(currentGridItems, currentDockItems)
    }

    private fun saveAndEmitState(gridItems: List<LauncherItem>, dockItems: List<LauncherItem>) {
        val maxPage = gridItems.maxOfOrNull { it.position.page } ?: 0
        val pagesMap = gridItems.groupBy { it.position.page }

        _state.update {
            it.copy(
                pages = pagesMap,
                totalPages = maxPage + 1,
                dockItems = dockItems
            )
        }

        viewModelScope.launch {
            homePreferences.saveGridState(gridItems)
            homePreferences.saveDockState(dockItems)
        }
    }

    fun launchApp(packageName: String) {
        installedAppsManager.launchApp(packageName)
    }
}

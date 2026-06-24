package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.AppSortType
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
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
    private val homePreferences: HomePreferences
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
                    } else {
                        installedApps.find { it.id == savedItem.id }?.let {
                            finalDockItems.add(it.copy(position = GridPosition(savedItem.page, savedItem.row, savedItem.column)))
                        }
                    }
                }
            }

            // Load Grid
            val dockIds = finalDockItems.flatMap { if (it is LauncherItem.FolderItem) it.apps.map { a -> a.id } else listOf(it.id) }.toSet()

            if (savedState.isEmpty()) {
                // Auto-place apps not in dock
                val gridApps = installedApps.filter { it.id !in dockIds }
                gridApps.forEachIndexed { index, app ->
                    // Page 0 rows 0 and 1 are reserved for the Clock Widget
                    val adjustedIndex = index + (columns * 2)

                    val page = adjustedIndex / itemsPerPage
                    val row = (adjustedIndex % itemsPerPage) / columns
                    val column = (adjustedIndex % itemsPerPage) % columns
                    finalItems.add(app.copy(position = GridPosition(page, row, column)))
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
                    } else {
                        installedApps.find { it.id == savedItem.id }?.let {
                            finalItems.add(it.copy(position = GridPosition(savedItem.page, savedItem.row, savedItem.column)))
                        }
                    }
                }

                val savedAppIds = savedState.flatMap { if (it.isFolder) it.folderContents ?: emptyList() else listOf(it.id) }.toSet()
                var nextEmpty = findNextEmptyCell(finalItems)
                installedApps.filter { it.id !in savedAppIds && it.id !in dockIds }.forEach { newApp ->
                    finalItems.add(newApp.copy(position = nextEmpty))
                    nextEmpty = findNextEmptyCell(finalItems + newApp.copy(position = nextEmpty))
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

    private fun findNextEmptyCell(currentItems: List<LauncherItem>): GridPosition {
        val maxPage = currentItems.maxOfOrNull { it.position.page } ?: 0
        for (page in 0..maxPage + 1) {
            for (row in 0 until rows) {
                // Reserve rows 0 and 1 on page 0 for Clock Widget
                if (page == 0 && row < 2) continue

                for (col in 0 until columns) {
                    if (currentItems.none { it.position.page == page && it.position.row == row && it.position.column == col }) {
                        return GridPosition(page, row, col)
                    }
                }
            }
        }
        return GridPosition(maxPage + 1, 0, 0)
    }

    fun onItemMoved(item: LauncherItem, newPosition: GridPosition) {
        // Prevent dropping onto the Clock Widget area on page 0
        if (newPosition.page == 0 && newPosition.row < 2) return

        val currentGridItems = _state.value.pages.values.flatten().toMutableList()
        val currentDockItems = _state.value.dockItems.toMutableList()

        currentGridItems.removeIf { it.id == item.id }
        currentDockItems.removeIf { it.id == item.id }

        val isTargetingDock = newPosition.row == -2

        val targetList = if (isTargetingDock) currentDockItems else currentGridItems
        val targetItem = targetList.find { it.position == newPosition }

        if (targetItem != null && targetItem.id != item.id) {
            targetList.removeIf { it.id == targetItem.id }

            if (targetItem is LauncherItem.FolderItem && item is LauncherItem.AppItem) {
                val newFolder = targetItem.copy(apps = targetItem.apps + item)
                targetList.add(newFolder)
            } else if (targetItem is LauncherItem.AppItem && item is LauncherItem.AppItem) {
                val newFolder = LauncherItem.FolderItem(
                    id = UUID.randomUUID().toString(),
                    name = "Folder",
                    apps = listOf(targetItem, item),
                    position = newPosition
                )
                targetList.add(newFolder)
            } else {
                targetList.add(item)
                targetList.add(targetItem)
            }
        } else {
            if (isTargetingDock && currentDockItems.size >= 5 && targetItem == null) {
                currentGridItems.add(item)
            } else {
                val updatedItem = when (item) {
                    is LauncherItem.AppItem -> item.copy(position = newPosition)
                    is LauncherItem.FolderItem -> item.copy(position = newPosition)
                }
                targetList.add(updatedItem)
            }
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

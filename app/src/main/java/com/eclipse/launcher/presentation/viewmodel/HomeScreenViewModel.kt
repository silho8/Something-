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
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // Using alphabetical sorting by default
            val installedApps = installedAppsManager.getInstalledApps(AppSortType.ALPHABETICAL_ASC)
            val savedState = homePreferences.getSavedGridState().first()

            val finalItems = mutableListOf<LauncherItem>()

            if (savedState.isEmpty()) {
                // First launch: Auto-place apps sequentially
                installedApps.forEachIndexed { index, app ->
                    val page = index / itemsPerPage
                    val row = (index % itemsPerPage) / columns
                    val column = (index % itemsPerPage) % columns
                    finalItems.add(app.copy(position = GridPosition(page, row, column)))
                }
            } else {
                // Restore saved layout
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

                // Add any newly installed apps that aren't in the saved state
                val savedAppIds = savedState.flatMap { if (it.isFolder) it.folderContents ?: emptyList() else listOf(it.id) }.toSet()
                var nextEmpty = findNextEmptyCell(finalItems)
                installedApps.filter { it.id !in savedAppIds }.forEach { newApp ->
                    finalItems.add(newApp.copy(position = nextEmpty))
                    nextEmpty = findNextEmptyCell(finalItems + newApp.copy(position = nextEmpty))
                }
            }

            // Calculate total pages
            val maxPage = finalItems.maxOfOrNull { it.position.page } ?: 0

            // Group by page
            val pagesMap = finalItems.groupBy { it.position.page }

            _state.update {
                it.copy(
                    isLoading = false,
                    pages = pagesMap,
                    totalPages = maxPage + 1
                )
            }
        }
    }

    private fun findNextEmptyCell(currentItems: List<LauncherItem>): GridPosition {
        val maxPage = currentItems.maxOfOrNull { it.position.page } ?: 0
        for (page in 0..maxPage + 1) {
            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    if (currentItems.none { it.position.page == page && it.position.row == row && it.position.column == col }) {
                        return GridPosition(page, row, col)
                    }
                }
            }
        }
        return GridPosition(0, 0, 0)
    }

    fun onItemMoved(item: LauncherItem, newPosition: GridPosition) {
        val currentItems = _state.value.pages.values.flatten().toMutableList()

        val targetItem = currentItems.find { it.position == newPosition }

        if (targetItem != null && targetItem.id != item.id) {
            currentItems.removeIf { it.id == item.id }
            currentItems.removeIf { it.id == targetItem.id }

            if (targetItem is LauncherItem.FolderItem && item is LauncherItem.AppItem) {
                val newFolder = targetItem.copy(apps = targetItem.apps + item)
                currentItems.add(newFolder)
            } else if (targetItem is LauncherItem.AppItem && item is LauncherItem.AppItem) {
                val newFolder = LauncherItem.FolderItem(
                    id = UUID.randomUUID().toString(),
                    name = "Folder",
                    apps = listOf(targetItem, item),
                    position = newPosition
                )
                currentItems.add(newFolder)
            } else {
                currentItems.add(item)
                currentItems.add(targetItem)
            }
        } else {
            currentItems.removeIf { it.id == item.id }
            val updatedItem = when (item) {
                is LauncherItem.AppItem -> item.copy(position = newPosition)
                is LauncherItem.FolderItem -> item.copy(position = newPosition)
            }
            currentItems.add(updatedItem)
        }

        saveAndEmitState(currentItems)
    }

    private fun saveAndEmitState(items: List<LauncherItem>) {
        val maxPage = items.maxOfOrNull { it.position.page } ?: 0
        val pagesMap = items.groupBy { it.position.page }

        _state.update {
            it.copy(
                pages = pagesMap,
                totalPages = maxPage + 1
            )
        }

        viewModelScope.launch {
            homePreferences.saveGridState(items)
        }
    }

    fun launchApp(packageName: String) {
        installedAppsManager.launchApp(packageName)
    }
}

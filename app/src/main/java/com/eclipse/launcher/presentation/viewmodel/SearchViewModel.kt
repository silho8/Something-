package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.domain.model.AppSortType
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.domain.repository.InstalledAppsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val installedAppsManager: InstalledAppsManager
) : ViewModel() {

    private val _allApps = MutableStateFlow<List<LauncherItem.AppItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<LauncherItem.AppItem>> = _allApps.combine(_searchQuery) { allApps, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            allApps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _allApps.update { installedAppsManager.getInstalledApps(AppSortType.ALPHABETICAL_ASC) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun launchApp(packageName: String) {
        installedAppsManager.launchApp(packageName)
    }
}

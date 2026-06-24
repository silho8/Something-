package com.eclipse.launcher.ui.home

import com.eclipse.launcher.domain.model.LauncherItem

data class HomeScreenState(
    val isLoading: Boolean = true,
    // A map where the key is the Page index, and the value is a list of items on that page.
    val pages: Map<Int, List<LauncherItem>> = emptyMap(),
    val totalPages: Int = 1
)

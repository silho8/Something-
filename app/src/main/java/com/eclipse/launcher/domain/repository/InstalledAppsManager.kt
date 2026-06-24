package com.eclipse.launcher.domain.repository

import com.eclipse.launcher.domain.model.AppSortType
import com.eclipse.launcher.domain.model.LauncherItem

interface InstalledAppsManager {
    suspend fun getInstalledApps(sortType: AppSortType = AppSortType.ALPHABETICAL_ASC, forceRefresh: Boolean = false): List<LauncherItem.AppItem>
    fun launchApp(packageName: String): Boolean
}

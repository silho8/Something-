package com.eclipse.launcher.domain.repository

import com.eclipse.launcher.domain.model.LauncherItem

interface AppRepository {
    suspend fun getInstalledApps(): List<LauncherItem.AppItem>
}

package com.eclipse.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.eclipse.launcher.domain.model.AppSortType
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.domain.repository.InstalledAppsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InstalledAppsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledAppsManager {

    private val cachedApps = mutableListOf<LauncherItem.AppItem>()

    override suspend fun getInstalledApps(
        sortType: AppSortType,
        forceRefresh: Boolean
    ): List<LauncherItem.AppItem> = withContext(Dispatchers.IO) {
        if (cachedApps.isEmpty() || forceRefresh) {
            refreshCache()
        }

        when (sortType) {
            AppSortType.ALPHABETICAL_ASC -> cachedApps.sortedBy { it.label.lowercase() }
            AppSortType.ALPHABETICAL_DESC -> cachedApps.sortedByDescending { it.label.lowercase() }
            AppSortType.INSTALL_DATE_DESC -> cachedApps.sortedByDescending { it.installTime }
            AppSortType.INSTALL_DATE_ASC -> cachedApps.sortedBy { it.installTime }
        }
    }

    private fun refreshCache() {
        cachedApps.clear()

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        resolveInfos.forEach { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val className = resolveInfo.activityInfo.name
            val label = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)

            val installTime = try {
                val packageInfo = pm.getPackageInfo(packageName, 0)
                packageInfo.firstInstallTime
            } catch (e: PackageManager.NameNotFoundException) {
                0L
            }

            val id = "$packageName/$className"

            val appItem = LauncherItem.AppItem(
                id = id,
                packageName = packageName,
                className = className,
                label = label,
                icon = icon,
                installTime = installTime,
                position = GridPosition(page = 0, row = -1, column = -1) // Temporary placement
            )

            cachedApps.add(appItem)
        }
    }

    override fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        }
        return false
    }
}

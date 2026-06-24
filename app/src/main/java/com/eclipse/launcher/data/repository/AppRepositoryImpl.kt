package com.eclipse.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    override suspend fun getInstalledApps(): List<LauncherItem.AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Use resolveActivity flags suitable for modern Android
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        resolveInfos.mapIndexed { index, resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val className = resolveInfo.activityInfo.name
            val label = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)

            // Temporary assignment of position for newly loaded apps (handled later by ViewModel/Preferences)
            val id = "$packageName/$className"

            LauncherItem.AppItem(
                id = id,
                packageName = packageName,
                className = className,
                label = label,
                icon = icon,
                position = GridPosition(page = 0, row = -1, column = -1) // Unassigned position
            )
        }
    }
}

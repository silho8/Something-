package com.eclipse.launcher.domain.model

import android.graphics.drawable.Drawable

sealed class LauncherItem {
    abstract val id: String
    abstract val position: GridPosition

    data class AppItem(
        override val id: String, // Usually package name + class name
        val packageName: String,
        val className: String,
        val label: String,
        val icon: Drawable? = null,
        val installTime: Long = 0L,
        override val position: GridPosition
    ) : LauncherItem()

    data class FolderItem(
        override val id: String, // Unique UUID for the folder
        val name: String,
        val apps: List<AppItem>,
        override val position: GridPosition
    ) : LauncherItem()
}

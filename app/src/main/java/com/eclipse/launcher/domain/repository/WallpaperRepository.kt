package com.eclipse.launcher.domain.repository

import android.net.Uri

interface WallpaperRepository {
    suspend fun saveWallpaper(uri: Uri): String?
}

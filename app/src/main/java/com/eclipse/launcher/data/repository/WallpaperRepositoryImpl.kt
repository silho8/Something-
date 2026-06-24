package com.eclipse.launcher.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.eclipse.launcher.domain.repository.WallpaperRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class WallpaperRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WallpaperRepository {

    override suspend fun saveWallpaper(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                // Save cropped/original bitmap to internal storage
                val file = File(context.filesDir, "custom_wallpaper.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                return@withContext file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

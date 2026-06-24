package com.eclipse.launcher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.LauncherItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "home_settings")

class HomePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val gson = Gson()

    companion object {
        val GRID_STATE_KEY = stringPreferencesKey("grid_state")
        val DOCK_STATE_KEY = stringPreferencesKey("dock_state")
        val WALLPAPER_PATH_KEY = stringPreferencesKey("wallpaper_path")
    }

    data class SavedItemPosition(
        val id: String,
        val page: Int,
        val row: Int,
        val column: Int,
        val isFolder: Boolean = false,
        val folderName: String? = null,
        val folderContents: List<String>? = null
    )

    fun getSavedGridState(): Flow<List<SavedItemPosition>> = context.homeDataStore.data.map { preferences ->
        val json = preferences[GRID_STATE_KEY] ?: "[]"
        val type = object : TypeToken<List<SavedItemPosition>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun saveGridState(items: List<LauncherItem>) {
        val savedItems = serializeItems(items)
        val json = gson.toJson(savedItems)
        context.homeDataStore.edit { preferences ->
            preferences[GRID_STATE_KEY] = json
        }
    }

    fun getSavedDockState(): Flow<List<SavedItemPosition>> = context.homeDataStore.data.map { preferences ->
        val json = preferences[DOCK_STATE_KEY] ?: "[]"
        val type = object : TypeToken<List<SavedItemPosition>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun saveDockState(items: List<LauncherItem>) {
        val savedItems = serializeItems(items)
        val json = gson.toJson(savedItems)
        context.homeDataStore.edit { preferences ->
            preferences[DOCK_STATE_KEY] = json
        }
    }

    private fun serializeItems(items: List<LauncherItem>): List<SavedItemPosition> {
        return items.map { item ->
            when (item) {
                is LauncherItem.AppItem -> {
                    SavedItemPosition(
                        id = item.id,
                        page = item.position.page,
                        row = item.position.row,
                        column = item.position.column
                    )
                }
                is LauncherItem.FolderItem -> {
                    SavedItemPosition(
                        id = item.id,
                        page = item.position.page,
                        row = item.position.row,
                        column = item.position.column,
                        isFolder = true,
                        folderName = item.name,
                        folderContents = item.apps.map { it.id }
                    )
                }
            }
        }
    }

    fun getWallpaperPath(): Flow<String?> = context.homeDataStore.data.map { preferences ->
        preferences[WALLPAPER_PATH_KEY]
    }

    suspend fun saveWallpaperPath(path: String) {
        context.homeDataStore.edit { preferences ->
            preferences[WALLPAPER_PATH_KEY] = path
        }
    }
}

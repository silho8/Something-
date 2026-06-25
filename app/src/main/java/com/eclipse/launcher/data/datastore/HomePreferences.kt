package com.eclipse.launcher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eclipse.launcher.domain.model.Gesture
import com.eclipse.launcher.domain.model.GestureAction
import com.eclipse.launcher.domain.model.GridPosition
import com.eclipse.launcher.domain.model.IconStyle
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.domain.model.TypographyStyle
import com.eclipse.launcher.domain.model.WidgetSize
import com.eclipse.launcher.domain.model.WidgetType
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
        val ICON_STYLE_KEY = stringPreferencesKey("icon_style")

        val AMOLED_MODE_KEY = booleanPreferencesKey("amoled_mode")
        val BLUR_STRENGTH_KEY = floatPreferencesKey("blur_strength")
        val GLASS_DEPTH_KEY = floatPreferencesKey("glass_depth")
        val TYPOGRAPHY_STYLE_KEY = stringPreferencesKey("typography_style")

        val GESTURES_KEY = stringPreferencesKey("gestures_state")
    }

    data class SavedItemPosition(
        val id: String,
        val page: Int,
        val row: Int,
        val column: Int,
        val isFolder: Boolean = false,
        val folderName: String? = null,
        val folderContents: List<String>? = null,
        val isWidget: Boolean = false,
        val widgetType: String? = null,
        val spanCols: Int? = null,
        val spanRows: Int? = null
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
                is LauncherItem.WidgetItem -> {
                    SavedItemPosition(
                        id = item.id,
                        page = item.position.page,
                        row = item.position.row,
                        column = item.position.column,
                        isWidget = true,
                        widgetType = item.widgetType.name,
                        spanCols = item.size.spanColumns,
                        spanRows = item.size.spanRows
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

    fun getIconStyle(): Flow<IconStyle> = context.homeDataStore.data.map { preferences ->
        val styleString = preferences[ICON_STYLE_KEY] ?: IconStyle.GLASS.name
        try {
            IconStyle.valueOf(styleString)
        } catch (e: IllegalArgumentException) {
            IconStyle.GLASS
        }
    }

    suspend fun saveIconStyle(style: IconStyle) {
        context.homeDataStore.edit { preferences ->
            preferences[ICON_STYLE_KEY] = style.name
        }
    }

    // UI Appearance Settings
    fun getAmoledMode(): Flow<Boolean> = context.homeDataStore.data.map { preferences ->
        preferences[AMOLED_MODE_KEY] ?: true
    }

    suspend fun saveAmoledMode(enabled: Boolean) {
        context.homeDataStore.edit { preferences ->
            preferences[AMOLED_MODE_KEY] = enabled
        }
    }

    fun getBlurStrength(): Flow<Float> = context.homeDataStore.data.map { preferences ->
        preferences[BLUR_STRENGTH_KEY] ?: 25f
    }

    suspend fun saveBlurStrength(strength: Float) {
        context.homeDataStore.edit { preferences ->
            preferences[BLUR_STRENGTH_KEY] = strength
        }
    }

    fun getGlassDepth(): Flow<Float> = context.homeDataStore.data.map { preferences ->
        preferences[GLASS_DEPTH_KEY] ?: 0.4f
    }

    suspend fun saveGlassDepth(depth: Float) {
        context.homeDataStore.edit { preferences ->
            preferences[GLASS_DEPTH_KEY] = depth
        }
    }

    fun getTypographyStyle(): Flow<TypographyStyle> = context.homeDataStore.data.map { preferences ->
        val styleString = preferences[TYPOGRAPHY_STYLE_KEY] ?: TypographyStyle.NDOT_WIDGETS_ONLY.name
        try {
            TypographyStyle.valueOf(styleString)
        } catch (e: IllegalArgumentException) {
            TypographyStyle.NDOT_WIDGETS_ONLY
        }
    }

    suspend fun saveTypographyStyle(style: TypographyStyle) {
        context.homeDataStore.edit { preferences ->
            preferences[TYPOGRAPHY_STYLE_KEY] = style.name
        }
    }

    // Gestures
    fun getGestures(): Flow<Map<Gesture, GestureAction>> = context.homeDataStore.data.map { preferences ->
        val json = preferences[GESTURES_KEY] ?: "{}"
        val type = object : TypeToken<Map<String, String>>() {}.type
        val stringMap: Map<String, String>? = gson.fromJson(json, type)

        val defaultMap = mutableMapOf(
            Gesture.SWIPE_UP to GestureAction.OPEN_APP_DRAWER,
            Gesture.SWIPE_DOWN to GestureAction.EXPAND_NOTIFICATIONS,
            Gesture.DOUBLE_TAP to GestureAction.LOCK_DEVICE,
            Gesture.LONG_PRESS to GestureAction.OPEN_SETTINGS,
            Gesture.TWO_FINGER_SWIPE_DOWN to GestureAction.OPEN_SEARCH
        )

        if (stringMap.isNullOrEmpty()) {
            defaultMap
        } else {
            val loadedMap = mutableMapOf<Gesture, GestureAction>()
            stringMap.forEach { (k, v) ->
                try {
                    loadedMap[Gesture.valueOf(k)] = GestureAction.valueOf(v)
                } catch (e: Exception) { }
            }
            defaultMap.forEach { (k, v) ->
                if (!loadedMap.containsKey(k)) loadedMap[k] = v
            }
            loadedMap
        }
    }

    suspend fun saveGestures(gestures: Map<Gesture, GestureAction>) {
        val stringMap = gestures.mapKeys { it.key.name }.mapValues { it.value.name }
        val json = gson.toJson(stringMap)
        context.homeDataStore.edit { preferences ->
            preferences[GESTURES_KEY] = json
        }
    }
}

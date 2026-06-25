package com.eclipse.launcher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eclipse.launcher.domain.model.TypographyStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "typography_settings")

class TypographyPreferences(private val context: Context) {

    companion object {
        val TYPOGRAPHY_STYLE_KEY = stringPreferencesKey("typography_style")
    }

    val typographyStyle: Flow<TypographyStyle> = context.dataStore.data
        .map { preferences ->
            val styleName = preferences[TYPOGRAPHY_STYLE_KEY] ?: TypographyStyle.NDOT_WIDGETS_ONLY.name
            try {
                TypographyStyle.valueOf(styleName)
            } catch (e: IllegalArgumentException) {
                TypographyStyle.NDOT_WIDGETS_ONLY
            }
        }

    suspend fun setTypographyStyle(style: TypographyStyle) {
        context.dataStore.edit { preferences ->
            preferences[TYPOGRAPHY_STYLE_KEY] = style.name
        }
    }
}

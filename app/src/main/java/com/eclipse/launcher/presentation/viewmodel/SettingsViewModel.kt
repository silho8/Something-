package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.Gesture
import com.eclipse.launcher.domain.model.GestureAction
import com.eclipse.launcher.domain.model.IconStyle
import com.eclipse.launcher.domain.model.TypographyStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val homePreferences: HomePreferences
) : ViewModel() {

    val amoledMode: StateFlow<Boolean> = homePreferences.getAmoledMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val blurStrength: StateFlow<Float> = homePreferences.getBlurStrength()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 25f)

    val glassDepth: StateFlow<Float> = homePreferences.getGlassDepth()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.4f)

    val typographyStyle: StateFlow<TypographyStyle> = homePreferences.getTypographyStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, TypographyStyle.NDOT_WIDGETS_ONLY)

    val iconStyle: StateFlow<IconStyle> = homePreferences.getIconStyle()
        .stateIn(viewModelScope, SharingStarted.Eagerly, IconStyle.GLASS)

    val gestures: StateFlow<Map<Gesture, GestureAction>> = homePreferences.getGestures()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun updateAmoledMode(enabled: Boolean) {
        viewModelScope.launch { homePreferences.saveAmoledMode(enabled) }
    }

    fun updateBlurStrength(strength: Float) {
        viewModelScope.launch { homePreferences.saveBlurStrength(strength) }
    }

    fun updateGlassDepth(depth: Float) {
        viewModelScope.launch { homePreferences.saveGlassDepth(depth) }
    }

    fun updateTypographyStyle(style: TypographyStyle) {
        viewModelScope.launch { homePreferences.saveTypographyStyle(style) }
    }

    fun updateIconStyle(style: IconStyle) {
        viewModelScope.launch { homePreferences.saveIconStyle(style) }
    }

    fun updateGesture(gesture: Gesture, action: GestureAction) {
        viewModelScope.launch {
            val currentMap = gestures.value.toMutableMap()
            currentMap[gesture] = action
            homePreferences.saveGestures(currentMap)
        }
    }
}

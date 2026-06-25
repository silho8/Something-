package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.DynamicThemeColors
import com.eclipse.launcher.domain.model.IconStyle
import com.eclipse.launcher.domain.model.TypographyStyle
import com.eclipse.launcher.domain.repository.DynamicThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DynamicThemeViewModel @Inject constructor(
    dynamicThemeRepository: DynamicThemeRepository,
    private val homePreferences: HomePreferences
) : ViewModel() {

    val themeColors: StateFlow<DynamicThemeColors> = dynamicThemeRepository.getDynamicThemeColors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DynamicThemeColors()
        )

    val iconStyle: StateFlow<IconStyle> = homePreferences.getIconStyle()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = IconStyle.GLASS
        )

    val typographyStyle: StateFlow<TypographyStyle> = homePreferences.getTypographyStyle()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TypographyStyle.NDOT_WIDGETS_ONLY
        )

    val blurStrength: StateFlow<Float> = homePreferences.getBlurStrength()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 25f
        )

    val glassDepth: StateFlow<Float> = homePreferences.getGlassDepth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0.4f
        )

    fun setIconStyle(style: IconStyle) {
        viewModelScope.launch {
            homePreferences.saveIconStyle(style)
        }
    }
}

package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.model.DynamicThemeColors
import com.eclipse.launcher.domain.model.IconStyle
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

    // Exposed so that settings menus can call it later
    fun setIconStyle(style: IconStyle) {
        viewModelScope.launch {
            homePreferences.saveIconStyle(style)
        }
    }
}

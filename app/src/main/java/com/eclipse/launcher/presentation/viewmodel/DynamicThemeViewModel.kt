package com.eclipse.launcher.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.domain.model.DynamicThemeColors
import com.eclipse.launcher.domain.repository.DynamicThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DynamicThemeViewModel @Inject constructor(
    dynamicThemeRepository: DynamicThemeRepository
) : ViewModel() {

    val themeColors: StateFlow<DynamicThemeColors> = dynamicThemeRepository.getDynamicThemeColors()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DynamicThemeColors()
        )
}

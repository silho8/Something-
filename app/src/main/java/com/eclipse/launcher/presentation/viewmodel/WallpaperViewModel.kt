package com.eclipse.launcher.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.domain.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperState(
    val selectedUri: Uri? = null,
    val croppedUri: Uri? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val homePreferences: HomePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(WallpaperState())
    val state: StateFlow<WallpaperState> = _state.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        _state.update { it.copy(selectedUri = uri, saveSuccess = false) }
    }

    fun onImageCropped(uri: Uri?) {
        _state.update { it.copy(croppedUri = uri, saveSuccess = false) }
    }

    fun saveWallpaper() {
        val uri = _state.value.croppedUri ?: _state.value.selectedUri ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val savedPath = wallpaperRepository.saveWallpaper(uri)

            if (savedPath != null) {
                homePreferences.saveWallpaperPath(savedPath)
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } else {
                _state.update { it.copy(isSaving = false, saveSuccess = false) }
            }
        }
    }
}

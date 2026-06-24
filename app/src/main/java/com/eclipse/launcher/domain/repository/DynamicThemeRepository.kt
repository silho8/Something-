package com.eclipse.launcher.domain.repository

import com.eclipse.launcher.domain.model.DynamicThemeColors
import kotlinx.coroutines.flow.Flow

interface DynamicThemeRepository {
    fun getDynamicThemeColors(): Flow<DynamicThemeColors>
}

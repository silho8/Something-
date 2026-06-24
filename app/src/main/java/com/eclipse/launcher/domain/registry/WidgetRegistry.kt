package com.eclipse.launcher.domain.registry

import androidx.compose.runtime.Composable
import com.eclipse.launcher.domain.model.WidgetSize
import com.eclipse.launcher.domain.model.WidgetType

interface WidgetRegistry {
    fun getMinSize(type: WidgetType): WidgetSize
    fun getMaxSize(type: WidgetType): WidgetSize
    fun getDefaultSize(type: WidgetType): WidgetSize

    @Composable
    fun RenderWidget(type: WidgetType)
}

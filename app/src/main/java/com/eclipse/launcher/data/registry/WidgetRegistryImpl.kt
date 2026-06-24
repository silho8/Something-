package com.eclipse.launcher.data.registry

import androidx.compose.runtime.Composable
import com.eclipse.launcher.domain.model.WidgetSize
import com.eclipse.launcher.domain.model.WidgetType
import com.eclipse.launcher.domain.registry.WidgetRegistry
import com.eclipse.launcher.ui.home.components.BatteryWidget
import com.eclipse.launcher.ui.home.components.ClockWidget
import com.eclipse.launcher.ui.home.components.WeatherWidget
import javax.inject.Inject

class WidgetRegistryImpl @Inject constructor() : WidgetRegistry {

    override fun getMinSize(type: WidgetType): WidgetSize {
        return when (type) {
            WidgetType.CLOCK -> WidgetSize(3, 2)
            WidgetType.BATTERY -> WidgetSize(2, 2)
            WidgetType.WEATHER -> WidgetSize(3, 2)
        }
    }

    override fun getMaxSize(type: WidgetType): WidgetSize {
        return when (type) {
            WidgetType.CLOCK -> WidgetSize(5, 4)
            WidgetType.BATTERY -> WidgetSize(4, 3)
            WidgetType.WEATHER -> WidgetSize(5, 4)
        }
    }

    override fun getDefaultSize(type: WidgetType): WidgetSize {
        return when (type) {
            WidgetType.CLOCK -> WidgetSize(5, 2) // Full width default
            WidgetType.BATTERY -> WidgetSize(2, 2)
            WidgetType.WEATHER -> WidgetSize(4, 2)
        }
    }

    @Composable
    override fun RenderWidget(type: WidgetType) {
        when (type) {
            WidgetType.CLOCK -> ClockWidget()
            WidgetType.BATTERY -> BatteryWidget()
            WidgetType.WEATHER -> WeatherWidget()
        }
    }
}

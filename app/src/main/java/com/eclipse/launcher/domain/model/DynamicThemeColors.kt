package com.eclipse.launcher.domain.model

data class DynamicThemeColors(
    val dominantColor: Long? = null,
    val secondaryColor: Long? = null,
    val accentColor: Long? = null,
    val backgroundColor: Long? = null,
    val widgetColor: Long? = null,
    val dockColor: Long? = null,
    val iconTintColor: Long? = null
)

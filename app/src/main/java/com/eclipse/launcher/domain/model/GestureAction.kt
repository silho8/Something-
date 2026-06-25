package com.eclipse.launcher.domain.model

enum class GestureAction(val label: String) {
    NONE("None"),
    OPEN_APP_DRAWER("Open App Drawer"),
    OPEN_SEARCH("Open Search"),
    OPEN_SETTINGS("Open Settings"),
    LOCK_DEVICE("Lock Device"),
    TOGGLE_FOCUS_MODE("Toggle Focus Mode"),
    EXPAND_NOTIFICATIONS("Expand Notifications"),
    EXPAND_QUICK_SETTINGS("Expand Quick Settings"),
    LAUNCH_SELECTED_APP("Launch Selected App"),
    OPEN_WIDGET_GALLERY("Open Widget Gallery")
}

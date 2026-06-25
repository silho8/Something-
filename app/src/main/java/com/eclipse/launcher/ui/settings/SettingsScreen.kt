package com.eclipse.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eclipse.launcher.domain.model.Gesture
import com.eclipse.launcher.domain.model.GestureAction
import com.eclipse.launcher.domain.model.IconStyle
import com.eclipse.launcher.domain.model.TypographyStyle
import com.eclipse.launcher.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWallpaper: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val amoledMode by viewModel.amoledMode.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val glassDepth by viewModel.glassDepth.collectAsState()
    val typographyStyle by viewModel.typographyStyle.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val gestures by viewModel.gestures.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 32.dp)
    ) {
        Text(
            text = "Something.",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp).clickable { onNavigateBack() }
        )

        SettingsSectionHeader("Appearance")
        SettingsSwitchRow("AMOLED Mode", amoledMode) { viewModel.updateAmoledMode(it) }
        SettingsSliderRow("Blur Strength", blurStrength, 0f..100f) { viewModel.updateBlurStrength(it) }
        SettingsSliderRow("Glass Depth", glassDepth, 0f..1f) { viewModel.updateGlassDepth(it) }

        SettingsSectionHeader("Typography")
        SettingsDropdownRow("System Font", typographyStyle.name) {
            val options = TypographyStyle.values().map { it.name }
            DropdownPicker(options, typographyStyle.name) { selection ->
                viewModel.updateTypographyStyle(TypographyStyle.valueOf(selection))
            }
        }

        SettingsSectionHeader("Icons")
        SettingsDropdownRow("Icon Pack", iconStyle.name) {
            val options = IconStyle.values().map { it.name }
            DropdownPicker(options, iconStyle.name) { selection ->
                viewModel.updateIconStyle(IconStyle.valueOf(selection))
            }
        }

        SettingsSectionHeader("Wallpaper")
        SettingsClickableRow("Wallpaper Picker", "Change wallpaper") { onNavigateToWallpaper() }

        SettingsSectionHeader("Widgets")
        SettingsClickableRow("Widget Gallery", "Coming Soon") {}

        SettingsSectionHeader("Gestures")
        Gesture.values().forEach { gesture ->
            val action = gestures[gesture] ?: GestureAction.NONE
            SettingsDropdownRow(gesture.name.replace("_", " "), action.label) {
                val options = GestureAction.values().map { it.label }
                DropdownPicker(options, action.label) { selection ->
                    val selectedAction = GestureAction.values().first { it.label == selection }
                    viewModel.updateGesture(gesture, selectedAction)
                }
            }
        }

        SettingsSectionHeader("Advanced")
        SettingsClickableRow("Backup", "Export configuration") {}
        SettingsClickableRow("Restore", "Import configuration") {}
        SettingsClickableRow("Reset Launcher", "Restore defaults") {}

        SettingsSectionHeader("About")
        SettingsItemRow("Name", "Something.")
        SettingsItemRow("Version", "1.0.0")
        SettingsItemRow("Build Number", "1000")

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItemRow(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun SettingsClickableRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun SettingsSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun SettingsSliderRow(title: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsDropdownRow(title: String, currentValue: String, dropdownContent: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
        Text(text = currentValue, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        dropdownContent()
    }
}

@Composable
fun DropdownPicker(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = "▼",
            color = Color.White,
            modifier = Modifier.clickable { expanded = true }.padding(8.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

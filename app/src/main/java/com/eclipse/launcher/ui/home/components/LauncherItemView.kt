package com.eclipse.launcher.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eclipse.launcher.domain.model.LauncherItem
import com.eclipse.launcher.ui.home.LocalDragDropState
import com.eclipse.launcher.ui.theme.GlassContainer
import com.eclipse.launcher.ui.theme.StyledIcon

@Composable
fun LauncherItemView(
    item: LauncherItem,
    isDragging: Boolean = false,
    onAppClick: (String) -> Unit = {}
) {
    val dragDropState = LocalDragDropState.current

    val scale by animateFloatAsState(if (isDragging) 1.2f else 1.0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .pointerInput(item) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { _ ->
                        dragDropState.onDragStart(item)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDropState.onDrag(dragAmount)
                    },
                    onDragEnd = {
                        dragDropState.onDragEnd()
                    },
                    onDragCancel = {
                        dragDropState.onDragEnd()
                    }
                )
            }
            .clickable {
                if (item is LauncherItem.AppItem) {
                    onAppClick(item.packageName)
                }
            }
    ) {
        Spacer(modifier = Modifier.weight(1f))

        when (item) {
            is LauncherItem.AppItem -> {
                StyledIcon(
                    drawable = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            is LauncherItem.FolderItem -> {
                GlassContainer(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    transparency = 0.5f,
                    blurIntensity = 25.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${item.apps.size}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

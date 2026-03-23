package com.zxy.daydayplan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxy.daydayplan.domain.model.Priority
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import com.zxy.daydayplan.ui.theme.BrandPrimary
import com.zxy.daydayplan.ui.theme.DangerRed
import com.zxy.daydayplan.ui.theme.SuccessGreen
import com.zxy.daydayplan.ui.theme.WarningOrange

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isRestoreAction: Boolean = false,
    showEdit: Boolean = true,
    showDelete: Boolean = true
) {
    val toggleLabel = if (isRestoreAction) "恢复" else "完成"

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.width(74.dp),
                    color = BrandPrimary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Event, contentDescription = null, tint = BrandPrimary)
                        Text(text = item.startTime, style = MaterialTheme.typography.labelLarge, color = BrandPrimary)
                        DividerPill()
                        Text(
                            text = item.endTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TagPill(
                        text = item.formattedTimeRange(),
                        background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.recurringTemplateId != null) {
                        TagPill(
                            text = "每日固定项",
                            background = BrandPrimary.copy(alpha = 0.12f),
                            contentColor = BrandPrimary
                        )
                    }
                    if (item.note.isNotBlank()) {
                        Text(
                            text = item.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isRestoreAction) Icons.Outlined.Replay else Icons.Outlined.TaskAlt,
                        contentDescription = toggleLabel
                    )
                    Text(text = toggleLabel, modifier = Modifier.padding(start = 6.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (showEdit) {
                        FilledIconButton(
                            onClick = onEdit,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "编辑")
                        }
                    }
                    if (showDelete) {
                        FilledIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DangerRed.copy(alpha = 0.12f),
                                contentColor = DangerRed
                            )
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoCard(
    item: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isRestoreAction: Boolean = false,
    showEdit: Boolean = true,
    showDelete: Boolean = true,
    showReorder: Boolean = true,
    canMoveUp: Boolean = true,
    canMoveDown: Boolean = true
) {
    val toggleLabel = if (isRestoreAction) "恢复" else "完成"

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TagPill(
                                text = priorityLabel(item.priority),
                                background = priorityColor(item.priority).copy(alpha = 0.12f),
                                contentColor = priorityColor(item.priority)
                            )
                            item.dueTime?.let {
                                TagPill(
                                    text = "截止 $it",
                                    background = WarningOrange.copy(alpha = 0.12f),
                                    contentColor = WarningOrange
                                )
                            }
                            if (item.expectTodayCompletion) {
                                TagPill(
                                    text = "今日",
                                    background = SuccessGreen.copy(alpha = 0.12f),
                                    contentColor = SuccessGreen
                                )
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "#${item.sortOrder + 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isRestoreAction) Icons.Outlined.Replay else Icons.Outlined.TaskAlt,
                        contentDescription = toggleLabel
                    )
                    Text(toggleLabel, modifier = Modifier.padding(start = 6.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (showReorder) {
                        FilledIconButton(
                            onClick = onMoveUp,
                            enabled = canMoveUp,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Outlined.ArrowUpward, contentDescription = "上移")
                        }
                        FilledIconButton(
                            onClick = onMoveDown,
                            enabled = canMoveDown,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Outlined.ArrowDownward, contentDescription = "下移")
                        }
                    }
                    if (showEdit) {
                        FilledIconButton(
                            onClick = onEdit,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "编辑")
                        }
                    }
                    if (showDelete) {
                        FilledIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DangerRed.copy(alpha = 0.12f),
                                contentColor = DangerRed
                            )
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPill(
    text: String,
    background: Color,
    contentColor: Color
) {
    Surface(color = background, shape = MaterialTheme.shapes.medium) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
private fun DividerPill() {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(1.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ) {}
    }
}

private fun priorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> DangerRed
        Priority.MEDIUM -> WarningOrange
        Priority.LOW -> SuccessGreen
    }
}

private fun priorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "高优先级"
        Priority.MEDIUM -> "中优先级"
        Priority.LOW -> "低优先级"
    }
}

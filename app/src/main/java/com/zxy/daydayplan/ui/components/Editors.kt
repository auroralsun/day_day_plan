package com.zxy.daydayplan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxy.daydayplan.domain.model.Priority
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import com.zxy.daydayplan.domain.model.parseTimeOrNull
import com.zxy.daydayplan.ui.theme.BrandPrimary
import com.zxy.daydayplan.ui.theme.DangerRed
import com.zxy.daydayplan.ui.theme.SuccessGreen
import com.zxy.daydayplan.ui.theme.WarningOrange

private val reminderOptions = listOf(0, 5, 10, 15, 30, 60)

@Composable
fun ScheduleEditorDialog(
    initialValue: ScheduleItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        startTime: String,
        endTime: String,
        note: String,
        repeatDaily: Boolean,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int
    ) -> Unit
) {
    var title by remember(initialValue) { mutableStateOf(initialValue?.title.orEmpty()) }
    var startTime by remember(initialValue) { mutableStateOf(initialValue?.startTime ?: "09:00") }
    var endTime by remember(initialValue) { mutableStateOf(initialValue?.endTime ?: "10:00") }
    var note by remember(initialValue) { mutableStateOf(initialValue?.note.orEmpty()) }
    var repeatDaily by remember(initialValue) { mutableStateOf(initialValue?.recurringTemplateId != null) }
    var reminderEnabled by remember(initialValue) { mutableStateOf(initialValue?.reminderEnabled ?: false) }
    var reminderMinutesBefore by remember(initialValue) { mutableStateOf(initialValue?.reminderMinutesBefore ?: 10) }

    val errorMessage = remember(title, startTime, endTime) {
        validateScheduleInput(title, startTime, endTime)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (initialValue == null) "新增日程" else "编辑日程",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "支持每日固定项和开始前提醒。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditorField(
                    value = title,
                    onValueChange = { title = it },
                    label = "标题",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeWheelField(
                        label = "开始时间",
                        value = startTime,
                        placeholder = "选择时间",
                        modifier = Modifier.weight(1f),
                        onPick = { startTime = it }
                    )
                    TimeWheelField(
                        label = "结束时间",
                        value = endTime,
                        placeholder = "选择时间",
                        modifier = Modifier.weight(1f),
                        onPick = { endTime = it }
                    )
                }
                EditorField(
                    value = note,
                    onValueChange = { note = it },
                    label = "备注",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OptionRow(
                    title = "每日自动添加",
                    description = "开启后会按相同时间每天自动生成该日程。",
                    checked = repeatDaily,
                    onCheckedChange = { repeatDaily = it }
                )
                OptionRow(
                    title = "开始前提醒",
                    description = "在日程开始前发送通知提醒。",
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
                if (reminderEnabled) {
                    ReminderMinuteSelector(
                        selected = reminderMinutesBefore,
                        onSelect = { reminderMinutesBefore = it }
                    )
                }
                errorMessage?.let { ErrorMessage(it) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (errorMessage == null) {
                        onSave(
                            initialValue?.id ?: 0L,
                            title,
                            startTime,
                            endTime,
                            note,
                            repeatDaily,
                            reminderEnabled,
                            reminderMinutesBefore
                        )
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun TodoEditorDialog(
    initialValue: TodoItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        priority: Priority,
        expectTodayCompletion: Boolean,
        note: String,
        dueTime: String?,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int
    ) -> Unit
) {
    var title by remember(initialValue) { mutableStateOf(initialValue?.title.orEmpty()) }
    var priority by remember(initialValue) { mutableStateOf(initialValue?.priority ?: Priority.MEDIUM) }
    var expectTodayCompletion by remember(initialValue) {
        mutableStateOf(initialValue?.expectTodayCompletion ?: true)
    }
    var note by remember(initialValue) { mutableStateOf(initialValue?.note.orEmpty()) }
    var dueTime by remember(initialValue) { mutableStateOf(initialValue?.dueTime) }
    var reminderEnabled by remember(initialValue) { mutableStateOf(initialValue?.reminderEnabled ?: false) }
    var reminderMinutesBefore by remember(initialValue) { mutableStateOf(initialValue?.reminderMinutesBefore ?: 10) }

    val titleError = if (title.isBlank()) "标题不能为空" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (initialValue == null) "新增待办" else "编辑待办",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "支持截止时间和到期前提醒。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditorField(
                    value = title,
                    onValueChange = { title = it },
                    label = "标题",
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Priority.entries.forEach { option ->
                            FilterChip(
                                selected = priority == option,
                                onClick = { priority = option },
                                label = { Text(priorityLabel(option)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = priorityAccent(option).copy(alpha = 0.14f),
                                    selectedLabelColor = priorityAccent(option)
                                )
                            )
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = expectTodayCompletion,
                            onCheckedChange = { expectTodayCompletion = it }
                        )
                        Column {
                            Text(
                                text = "预计今天完成",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "用于区分今天优先处理的事项。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TimeWheelField(
                    label = "截止时间",
                    value = dueTime,
                    placeholder = "可选",
                    onPick = { dueTime = it },
                    onClear = { dueTime = null }
                )
                OptionRow(
                    title = "截止前提醒",
                    description = "在截止时间前发送通知提醒。",
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
                if (reminderEnabled) {
                    ReminderMinuteSelector(
                        selected = reminderMinutesBefore,
                        onSelect = { reminderMinutesBefore = it }
                    )
                }
                EditorField(
                    value = note,
                    onValueChange = { note = it },
                    label = "备注",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                titleError?.let { ErrorMessage(it) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titleError == null) {
                        onSave(
                            initialValue?.id ?: 0L,
                            title,
                            priority,
                            expectTodayCompletion,
                            note,
                            dueTime,
                            reminderEnabled,
                            reminderMinutesBefore
                        )
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun OptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderMinuteSelector(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "提醒时间",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reminderOptions.forEach { minute ->
                FilterChip(
                    selected = selected == minute,
                    onClick = { onSelect(minute) },
                    label = {
                        Text(if (minute == 0) "准时提醒" else "提前 $minute 分钟")
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = BrandPrimary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "·",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandPrimary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun validateScheduleInput(
    title: String,
    startTime: String,
    endTime: String
): String? {
    if (title.isBlank()) return "标题不能为空"
    val start = startTime.parseTimeOrNull() ?: return "开始时间格式必须是 HH:mm"
    val end = endTime.parseTimeOrNull() ?: return "结束时间格式必须是 HH:mm"
    if (!end.isAfter(start)) return "结束时间必须晚于开始时间"
    return null
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else Int.MAX_VALUE
    )
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = DangerRed
    )
}

private fun priorityAccent(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> DangerRed
        Priority.MEDIUM -> WarningOrange
        Priority.LOW -> SuccessGreen
    }
}

private fun priorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "高"
        Priority.MEDIUM -> "中"
        Priority.LOW -> "低"
    }
}

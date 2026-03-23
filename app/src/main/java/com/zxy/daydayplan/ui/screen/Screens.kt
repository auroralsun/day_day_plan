package com.zxy.daydayplan.ui.screen

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zxy.daydayplan.domain.model.CompletionStatus
import com.zxy.daydayplan.domain.model.DayPlan
import com.zxy.daydayplan.domain.model.Priority
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import com.zxy.daydayplan.ui.MainUiState
import com.zxy.daydayplan.ui.components.EmptyState
import com.zxy.daydayplan.ui.components.ScheduleCard
import com.zxy.daydayplan.ui.components.TodoCard
import com.zxy.daydayplan.ui.theme.BrandPrimary
import com.zxy.daydayplan.ui.theme.SuccessGreen
import com.zxy.daydayplan.ui.theme.WarningOrange
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OverviewScreen(state: MainUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { HeroCard(state) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "待完成日程",
                    value = "${state.pendingSchedules.size}",
                    subtitle = "当前和稍后仍可完成",
                    accent = BrandPrimary,
                    icon = Icons.Outlined.EventRepeat
                )
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "过时未完成",
                    value = "${state.overdueSchedules.size}",
                    subtitle = "已经过时但仍未完成",
                    accent = WarningOrange,
                    icon = Icons.Outlined.Insights
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "待办数量",
                    value = "${state.pendingTodos.size}",
                    subtitle = "今天尚未完成的待办",
                    accent = BrandPrimary,
                    icon = Icons.Outlined.Checklist
                )
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "已完成",
                    value = "${state.completedCount}",
                    subtitle = "今天已完成的日程和待办",
                    accent = SuccessGreen,
                    icon = Icons.Outlined.CheckCircleOutline
                )
            }
        }

        item {
            InsightCard(
                modifier = Modifier.fillMaxWidth(),
                title = "专注度",
                value = focusScore(state),
                subtitle = "根据今日完成情况自动计算",
                accent = SuccessGreen,
                icon = Icons.Outlined.Bolt
            )
        }
    }
}

@Composable
fun ScheduleScreen(
    schedules: List<ScheduleItem>,
    overdueSchedules: List<ScheduleItem>,
    onToggle: (Long) -> Unit,
    onEdit: (ScheduleItem) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (schedules.isEmpty() && overdueSchedules.isEmpty()) {
        EmptyState(
            title = "今天还没有日程",
            description = "点击下方加号，添加今天的时间安排。"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "今日日程",
                caption = "共 ${schedules.size + overdueSchedules.size} 项"
            )
        }

        if (schedules.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "待完成",
                    caption = "当前时间之后仍可完成"
                )
            }
            items(schedules, key = { item -> "pending-${item.id}" }) { item ->
                ScheduleCard(
                    item = item,
                    onToggle = { onToggle(item.id) },
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }

        if (overdueSchedules.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "过时未完成",
                    caption = "当前时间之前仍未完成"
                )
            }
            items(overdueSchedules, key = { item -> "overdue-${item.id}" }) { item ->
                ScheduleCard(
                    item = item,
                    onToggle = { onToggle(item.id) },
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }
    }
}

@Composable
fun TodoScreen(
    todos: List<TodoItem>,
    onToggle: (Long) -> Unit,
    onEdit: (TodoItem) -> Unit,
    onDelete: (Long) -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit
) {
    if (todos.isEmpty()) {
        EmptyState(
            title = "今天还没有待办",
            description = "点击下方加号，记录今天要完成的事情。"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "今日待办",
                caption = "支持截止时间提醒"
            )
        }
        itemsIndexed(todos, key = { _, item -> item.id }) { index, item ->
            TodoCard(
                item = item,
                onToggle = { onToggle(item.id) },
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item.id) },
                onMoveUp = { if (index > 0) onMoveUp(item.id) },
                onMoveDown = { if (index < todos.lastIndex) onMoveDown(item.id) },
                canMoveUp = index > 0,
                canMoveDown = index < todos.lastIndex
            )
        }
    }
}

@Composable
fun ReviewScreen(
    selectedDate: LocalDate,
    dayPlan: DayPlan,
    onSelectDate: (LocalDate) -> Unit
) {
    val pendingSchedules = dayPlan.schedules.filter { it.status == CompletionStatus.PENDING }
    val completedSchedules = dayPlan.schedules.filter { it.status == CompletionStatus.DONE }
    val pendingTodos = dayPlan.todos.filter { it.status == CompletionStatus.PENDING }
    val completedTodos = dayPlan.todos.filter { it.status == CompletionStatus.DONE }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE", Locale.SIMPLIFIED_CHINESE)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = BrandPrimary)
                        Text(
                            text = "往日日历回顾",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = selectedDate.format(formatter),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AndroidView(
                        factory = { context ->
                            CalendarView(context).apply {
                                date = selectedDate.toEpochMillis()
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    onSelectDate(LocalDate.of(year, month + 1, dayOfMonth))
                                }
                            }
                        },
                        update = { view ->
                            if (view.date != selectedDate.toEpochMillis()) {
                                view.date = selectedDate.toEpochMillis()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "日程",
                    value = "${dayPlan.schedules.size}",
                    subtitle = "已完成 ${completedSchedules.size} / 未完成 ${pendingSchedules.size}",
                    accent = BrandPrimary,
                    icon = Icons.Outlined.EventRepeat
                )
                InsightCard(
                    modifier = Modifier.weight(1f),
                    title = "待办",
                    value = "${dayPlan.todos.size}",
                    subtitle = "已完成 ${completedTodos.size} / 未完成 ${pendingTodos.size}",
                    accent = WarningOrange,
                    icon = Icons.Outlined.Checklist
                )
            }
        }

        item {
            SectionHeader(
                title = "当日日程",
                caption = if (dayPlan.schedules.isEmpty()) "没有记录" else "共 ${dayPlan.schedules.size} 项"
            )
        }
        if (dayPlan.schedules.isEmpty()) {
            item {
                EmptyState(
                    title = "这一天没有日程",
                    description = "可以切换其他日期查看整体回顾。"
                )
            }
        } else {
            items(dayPlan.schedules, key = { "review-schedule-${it.id}" }) { item ->
                ReviewScheduleCard(item)
            }
        }

        item {
            SectionHeader(
                title = "当日待办",
                caption = if (dayPlan.todos.isEmpty()) "没有记录" else "共 ${dayPlan.todos.size} 项"
            )
        }
        if (dayPlan.todos.isEmpty()) {
            item {
                EmptyState(
                    title = "这一天没有待办",
                    description = "选择其他日期继续查看。"
                )
            }
        } else {
            items(dayPlan.todos, key = { "review-todo-${it.id}" }) { item ->
                ReviewTodoCard(item)
            }
        }
    }
}

@Composable
fun CompletedScreen(
    schedules: List<ScheduleItem>,
    todos: List<TodoItem>,
    onRestoreSchedule: (Long) -> Unit,
    onRestoreTodo: (Long) -> Unit
) {
    if (schedules.isEmpty() && todos.isEmpty()) {
        EmptyState(
            title = "还没有已完成内容",
            description = "完成的日程和待办会显示在这里。"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "今日已完成",
                caption = "共 ${schedules.size + todos.size} 项"
            )
        }

        if (schedules.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "日程",
                    caption = "点击恢复可重新打开"
                )
            }
            items(schedules, key = { "done-schedule-${it.id}" }) { item ->
                ScheduleCard(
                    item = item,
                    onToggle = { onRestoreSchedule(item.id) },
                    onEdit = {},
                    onDelete = {},
                    isRestoreAction = true,
                    showEdit = false,
                    showDelete = false
                )
            }
        }

        if (todos.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "待办",
                    caption = "点击恢复可重新打开"
                )
            }
            items(todos, key = { "done-todo-${it.id}" }) { item ->
                TodoCard(
                    item = item,
                    onToggle = { onRestoreTodo(item.id) },
                    onEdit = {},
                    onDelete = {},
                    onMoveUp = {},
                    onMoveDown = {},
                    isRestoreAction = true,
                    showEdit = false,
                    showDelete = false,
                    showReorder = false
                )
            }
        }
    }
}

@Composable
private fun HeroCard(state: MainUiState) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BrandPrimary,
                            BrandPrimary.copy(alpha = 0.84f),
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Bolt, contentDescription = null, tint = Color.White)
                    Text(
                        text = "今日概览",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE", Locale.SIMPLIFIED_CHINESE)
            Text(
                text = state.dayPlan.date.format(formatter),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.ongoingSchedule?.let { "当前进行中：${it.title}" }
                    ?: "固定项、提醒和完成进度都会汇总在这里。",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(color = accent.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = accent
                )
            }
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewScheduleCard(item: ScheduleItem) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(item.formattedTimeRange(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (item.status == CompletionStatus.DONE) "状态：已完成" else "状态：未完成",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.status == CompletionStatus.DONE) SuccessGreen else WarningOrange
            )
            if (item.recurringTemplateId != null) {
                Text(
                    text = "固定项：每日自动添加",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.note.isNotBlank()) {
                Text(item.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ReviewTodoCard(item: TodoItem) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "优先级：${priorityLabel(item.priority)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.dueTime?.let {
                Text(
                    text = "截止时间：$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (item.status == CompletionStatus.DONE) "状态：已完成" else "状态：未完成",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.status == CompletionStatus.DONE) SuccessGreen else WarningOrange
            )
            if (item.note.isNotBlank()) {
                Text(item.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    caption: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    }
}

private fun focusScore(state: MainUiState): String {
    val total = state.pendingSchedules.size + state.overdueSchedules.size + state.pendingTodos.size + state.completedCount
    if (total == 0) return "--"
    val percent = (state.completedCount * 100f / total).toInt()
    return "$percent%"
}

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun priorityLabel(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "高"
        Priority.MEDIUM -> "中"
        Priority.LOW -> "低"
    }
}

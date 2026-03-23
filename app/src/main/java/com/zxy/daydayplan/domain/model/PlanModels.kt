package com.zxy.daydayplan.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class CompletionStatus {
    PENDING,
    DONE
}

enum class Priority(val level: Int, val label: String) {
    HIGH(3, "High"),
    MEDIUM(2, "Medium"),
    LOW(1, "Low");

    companion object {
        fun fromLevel(level: Int): Priority {
            return entries.firstOrNull { it.level == level } ?: MEDIUM
        }
    }
}

enum class PlanItemType {
    SCHEDULE,
    TODO
}

data class ScheduleItem(
    val id: Long = 0L,
    val date: LocalDate,
    val title: String,
    val startTime: String,
    val endTime: String,
    val note: String = "",
    val status: CompletionStatus = CompletionStatus.PENDING,
    val recurringTemplateId: Long? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 10
) {
    fun isOverdue(now: LocalTime): Boolean {
        val end = endTime.parseTimeOrNull() ?: return false
        return status == CompletionStatus.PENDING && now.isAfter(end)
    }

    fun isPendingNowOrLater(now: LocalTime): Boolean {
        return status == CompletionStatus.PENDING && !isOverdue(now)
    }

    fun isOngoing(now: LocalTime): Boolean {
        val start = startTime.parseTimeOrNull() ?: return false
        val end = endTime.parseTimeOrNull() ?: return false
        return status == CompletionStatus.PENDING && !now.isBefore(start) && !now.isAfter(end)
    }

    fun formattedTimeRange(): String = "$startTime - $endTime"
}

data class TodoItem(
    val id: Long = 0L,
    val date: LocalDate,
    val title: String,
    val priority: Priority = Priority.MEDIUM,
    val sortOrder: Int = 0,
    val expectTodayCompletion: Boolean = true,
    val note: String = "",
    val status: CompletionStatus = CompletionStatus.PENDING,
    val dueTime: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 10
)

data class DailyScheduleTemplate(
    val id: Long = 0L,
    val title: String,
    val startTime: String,
    val endTime: String,
    val note: String = "",
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 10,
    val active: Boolean = true
)

data class DayPlan(
    val date: LocalDate,
    val schedules: List<ScheduleItem>,
    val todos: List<TodoItem>
)

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun String.parseTimeOrNull(): LocalTime? {
    return runCatching { LocalTime.parse(this, timeFormatter) }.getOrNull()
}

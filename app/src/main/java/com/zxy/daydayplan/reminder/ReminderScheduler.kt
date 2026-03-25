package com.zxy.daydayplan.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import android.content.Context
import android.content.Intent
import com.zxy.daydayplan.domain.model.CompletionStatus
import com.zxy.daydayplan.domain.model.DailyScheduleTemplate
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleScheduleReminder(item: ScheduleItem) {
        cancelScheduleReminder(item.id)
        if (!item.reminderEnabled || item.status == CompletionStatus.DONE) return

        val start = item.startTime.toLocalTimeOrNull() ?: return
        val triggerAt = LocalDateTime.of(item.date, start)
            .minusMinutes(item.reminderMinutesBefore.toLong())
        if (triggerAt.isBefore(LocalDateTime.now())) return

        scheduleExact(
            requestCode = scheduleRequestCode(item.id),
            triggerAt = triggerAt,
            intent = ReminderReceiver.scheduleIntent(
                context = context,
                title = "日程提醒",
                message = "${item.title} 将在 ${item.startTime} 开始"
            )
        )
    }

    fun cancelScheduleReminder(id: Long) {
        cancel(scheduleRequestCode(id), ReminderReceiver.scheduleIntent(context, "", ""))
    }

    fun scheduleRecurringTemplate(template: DailyScheduleTemplate) {
        cancelRecurringTemplate(template.id)
        if (!template.reminderEnabled || !template.active) return

        val startTime = template.startTime.toLocalTimeOrNull() ?: return
        val now = LocalDateTime.now()
        val todayReminder = LocalDateTime.of(LocalDate.now(), startTime)
            .minusMinutes(template.reminderMinutesBefore.toLong())
        val triggerAt = if (todayReminder.isAfter(now)) todayReminder else todayReminder.plusDays(1)

        scheduleExact(
            requestCode = recurringRequestCode(template.id),
            triggerAt = triggerAt,
            intent = ReminderReceiver.recurringScheduleIntent(
                context = context,
                templateId = template.id,
                title = template.title,
                startTime = template.startTime,
                reminderMinutesBefore = template.reminderMinutesBefore
            )
        )
    }

    fun cancelRecurringTemplate(id: Long) {
        cancel(
            recurringRequestCode(id),
            ReminderReceiver.recurringScheduleIntent(
                context = context,
                templateId = id,
                title = "",
                startTime = "09:00",
                reminderMinutesBefore = 10
            )
        )
    }

    fun scheduleTodoReminder(item: TodoItem) {
        cancelTodoReminder(item.id)
        if (!item.reminderEnabled || item.status == CompletionStatus.DONE) return

        val dueTime = item.dueTime?.toLocalTimeOrNull() ?: return
        val triggerAt = LocalDateTime.of(item.date, dueTime)
            .minusMinutes(item.reminderMinutesBefore.toLong())
        if (triggerAt.isBefore(LocalDateTime.now())) return

        scheduleExact(
            requestCode = todoRequestCode(item.id),
            triggerAt = triggerAt,
            intent = ReminderReceiver.todoIntent(
                context = context,
                title = "待办截止提醒",
                message = "${item.title} 将在 ${item.dueTime} 到期"
            )
        )
    }

    fun cancelTodoReminder(id: Long) {
        cancel(todoRequestCode(id), ReminderReceiver.todoIntent(context, "", ""))
    }

    private fun scheduleExact(requestCode: Int, triggerAt: LocalDateTime, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (canScheduleExactAlarms()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancel(requestCode: Int, intent: Intent) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleRequestCode(id: Long): Int = ("schedule-$id").hashCode()
    private fun recurringRequestCode(id: Long): Int = ("recurring-$id").hashCode()
    private fun todoRequestCode(id: Long): Int = ("todo-$id").hashCode()

    private fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }
}

private fun String.toLocalTimeOrNull(): LocalTime? {
    return runCatching { LocalTime.parse(this) }.getOrNull()
}

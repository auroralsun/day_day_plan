package com.zxy.daydayplan.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zxy.daydayplan.MainActivity
import com.zxy.daydayplan.R
import com.zxy.daydayplan.domain.model.DailyScheduleTemplate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createChannel(context)
        if (!canPostNotifications(context)) return

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val message = intent.getStringExtra(EXTRA_MESSAGE).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode())

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)

        if (intent.getBooleanExtra(EXTRA_RECURRING, false)) {
            val templateId = intent.getLongExtra(EXTRA_TEMPLATE_ID, 0L)
            val startTime = intent.getStringExtra(EXTRA_START_TIME).orEmpty()
            val reminderMinutesBefore = intent.getIntExtra(EXTRA_REMINDER_MINUTES_BEFORE, 10)
            if (templateId != 0L && startTime.isNotBlank()) {
                ReminderScheduler(context).scheduleRecurringTemplate(
                    DailyScheduleTemplate(
                        id = templateId,
                        title = intent.getStringExtra(EXTRA_TEMPLATE_TITLE).orEmpty(),
                        startTime = startTime,
                        endTime = startTime,
                        reminderEnabled = true,
                        reminderMinutesBefore = reminderMinutesBefore
                    )
                )
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "day_day_plan_reminders"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_MESSAGE = "message"
        private const val EXTRA_NOTIFICATION_ID = "notification_id"
        private const val EXTRA_RECURRING = "recurring"
        private const val EXTRA_TEMPLATE_ID = "template_id"
        private const val EXTRA_TEMPLATE_TITLE = "template_title"
        private const val EXTRA_START_TIME = "start_time"
        private const val EXTRA_REMINDER_MINUTES_BEFORE = "reminder_minutes_before"

        fun scheduleIntent(context: Context, title: String, message: String): Intent {
            return baseIntent(context, title, message)
        }

        fun todoIntent(context: Context, title: String, message: String): Intent {
            return baseIntent(context, title, message)
        }

        fun recurringScheduleIntent(
            context: Context,
            templateId: Long,
            title: String,
            startTime: String,
            reminderMinutesBefore: Int
        ): Intent {
            return baseIntent(
                context = context,
                title = "固定日程提醒",
                message = "$title 将在 $startTime 开始"
            ).apply {
                putExtra(EXTRA_RECURRING, true)
                putExtra(EXTRA_TEMPLATE_ID, templateId)
                putExtra(EXTRA_TEMPLATE_TITLE, title)
                putExtra(EXTRA_START_TIME, startTime)
                putExtra(EXTRA_REMINDER_MINUTES_BEFORE, reminderMinutesBefore)
                putExtra(EXTRA_NOTIFICATION_ID, ("recurring-$templateId").hashCode())
            }
        }

        private fun baseIntent(context: Context, title: String, message: String): Intent {
            return Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_NOTIFICATION_ID, "$title$message".hashCode())
            }
        }

        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "计划提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "用于日程和待办提醒"
                }
            )
        }

        private fun canPostNotifications(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

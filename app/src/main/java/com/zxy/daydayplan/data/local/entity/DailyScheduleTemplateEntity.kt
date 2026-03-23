package com.zxy.daydayplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_schedule_templates")
data class DailyScheduleTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val startTime: String,
    val endTime: String,
    val note: String,
    val reminderEnabled: Boolean,
    val reminderMinutesBefore: Int,
    val active: Boolean
)

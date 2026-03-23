package com.zxy.daydayplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val note: String,
    val status: String,
    val recurringTemplateId: Long?,
    val reminderEnabled: Boolean,
    val reminderMinutesBefore: Int
)

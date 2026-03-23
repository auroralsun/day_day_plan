package com.zxy.daydayplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val title: String,
    val priority: Int,
    val sortOrder: Int,
    val expectTodayCompletion: Boolean,
    val note: String,
    val status: String,
    val dueTime: String?,
    val reminderEnabled: Boolean,
    val reminderMinutesBefore: Int
)

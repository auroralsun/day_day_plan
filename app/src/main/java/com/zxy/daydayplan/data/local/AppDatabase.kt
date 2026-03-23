package com.zxy.daydayplan.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zxy.daydayplan.data.local.dao.DailyScheduleTemplateDao
import com.zxy.daydayplan.data.local.dao.ScheduleDao
import com.zxy.daydayplan.data.local.dao.TodoDao
import com.zxy.daydayplan.data.local.entity.DailyScheduleTemplateEntity
import com.zxy.daydayplan.data.local.entity.ScheduleEntity
import com.zxy.daydayplan.data.local.entity.TodoEntity

@Database(
    entities = [ScheduleEntity::class, TodoEntity::class, DailyScheduleTemplateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyScheduleTemplateDao(): DailyScheduleTemplateDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun todoDao(): TodoDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE schedule_items
                    ADD COLUMN recurringTemplateId INTEGER
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE schedule_items
                    ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE schedule_items
                    ADD COLUMN reminderMinutesBefore INTEGER NOT NULL DEFAULT 10
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE todo_items
                    ADD COLUMN dueTime TEXT
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE todo_items
                    ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE todo_items
                    ADD COLUMN reminderMinutesBefore INTEGER NOT NULL DEFAULT 10
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_schedule_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        note TEXT NOT NULL,
                        reminderEnabled INTEGER NOT NULL,
                        reminderMinutesBefore INTEGER NOT NULL,
                        active INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}

package com.zxy.daydayplan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zxy.daydayplan.data.local.entity.DailyScheduleTemplateEntity

@Dao
interface DailyScheduleTemplateDao {
    @Query("SELECT * FROM daily_schedule_templates WHERE active = 1 ORDER BY startTime ASC, id ASC")
    suspend fun getActiveTemplates(): List<DailyScheduleTemplateEntity>

    @Query("SELECT * FROM daily_schedule_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DailyScheduleTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DailyScheduleTemplateEntity): Long

    @Update
    suspend fun update(item: DailyScheduleTemplateEntity)

    @Query("DELETE FROM daily_schedule_templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}

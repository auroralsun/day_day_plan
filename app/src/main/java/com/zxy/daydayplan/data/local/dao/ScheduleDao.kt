package com.zxy.daydayplan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zxy.daydayplan.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_items WHERE date = :date ORDER BY startTime ASC, id ASC")
    fun observeScheduleItems(date: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule_items WHERE date = :date ORDER BY startTime ASC, id ASC")
    suspend fun getScheduleItems(date: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedule_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduleEntity?

    @Query("SELECT * FROM schedule_items WHERE recurringTemplateId = :templateId AND date = :date LIMIT 1")
    suspend fun getByTemplateAndDate(templateId: Long, date: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScheduleEntity): Long

    @Update
    suspend fun update(item: ScheduleEntity)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM schedule_items WHERE recurringTemplateId = :templateId AND date >= :fromDate")
    suspend fun deleteFutureByTemplateId(templateId: Long, fromDate: String)
}

package com.zxy.daydayplan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zxy.daydayplan.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items WHERE date = :date ORDER BY status ASC, priority DESC, sortOrder ASC, id ASC")
    fun observeTodoItems(date: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo_items WHERE date = :date ORDER BY status ASC, priority DESC, sortOrder ASC, id ASC")
    suspend fun getTodoItems(date: String): List<TodoEntity>

    @Query("SELECT * FROM todo_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TodoEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM todo_items WHERE date = :date")
    suspend fun getMaxSortOrder(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoEntity): Long

    @Update
    suspend fun update(item: TodoEntity)

    @Update
    suspend fun updateAll(items: List<TodoEntity>)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun reorderByIds(ids: List<Long>) {
        val items = ids.mapIndexedNotNull { index, id ->
            getById(id)?.copy(sortOrder = index)
        }
        if (items.isNotEmpty()) {
            updateAll(items)
        }
    }
}

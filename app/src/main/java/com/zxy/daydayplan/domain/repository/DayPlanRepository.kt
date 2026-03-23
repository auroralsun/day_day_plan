package com.zxy.daydayplan.domain.repository

import com.zxy.daydayplan.domain.model.DayPlan
import com.zxy.daydayplan.domain.model.DailyScheduleTemplate
import com.zxy.daydayplan.domain.model.PlanItemType
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface DayPlanRepository {
    fun getDayPlan(date: LocalDate): Flow<DayPlan>

    suspend fun addScheduleItem(item: ScheduleItem)

    suspend fun updateScheduleItem(item: ScheduleItem)

    suspend fun addDailyScheduleTemplate(template: DailyScheduleTemplate): Long

    suspend fun updateDailyScheduleTemplate(template: DailyScheduleTemplate)

    suspend fun deleteDailyScheduleTemplate(id: Long, fromDate: LocalDate)

    suspend fun deleteScheduleItem(id: Long)

    suspend fun addTodoItem(item: TodoItem)

    suspend fun updateTodoItem(item: TodoItem)

    suspend fun reorderTodoItems(ids: List<Long>)

    suspend fun deleteTodoItem(id: Long)

    suspend fun toggleItemCompletion(id: Long, type: PlanItemType)
}

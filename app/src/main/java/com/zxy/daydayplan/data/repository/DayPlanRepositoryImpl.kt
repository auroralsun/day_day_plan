package com.zxy.daydayplan.data.repository

import com.zxy.daydayplan.data.local.dao.DailyScheduleTemplateDao
import com.zxy.daydayplan.data.local.dao.ScheduleDao
import com.zxy.daydayplan.data.local.dao.TodoDao
import com.zxy.daydayplan.data.local.entity.DailyScheduleTemplateEntity
import com.zxy.daydayplan.data.local.entity.ScheduleEntity
import com.zxy.daydayplan.data.local.entity.TodoEntity
import com.zxy.daydayplan.domain.model.CompletionStatus
import com.zxy.daydayplan.domain.model.DailyScheduleTemplate
import com.zxy.daydayplan.domain.model.DayPlan
import com.zxy.daydayplan.domain.model.PlanItemType
import com.zxy.daydayplan.domain.model.Priority
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import com.zxy.daydayplan.domain.repository.DayPlanRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

class DayPlanRepositoryImpl(
    private val scheduleDao: ScheduleDao,
    private val todoDao: TodoDao,
    private val dailyScheduleTemplateDao: DailyScheduleTemplateDao
) : DayPlanRepository {

    override fun getDayPlan(date: LocalDate): Flow<DayPlan> {
        val key = date.toString()
        return combine(
            scheduleDao.observeScheduleItems(key),
            todoDao.observeTodoItems(key)
        ) { schedules, todos ->
            DayPlan(
                date = date,
                schedules = schedules.map { it.toDomain() },
                todos = todos.map { it.toDomain() }
            )
        }.onStart {
            ensureDailySchedulesForDate(date)
        }
    }

    override suspend fun addScheduleItem(item: ScheduleItem): Long {
        return scheduleDao.insert(item.toEntity())
    }

    override suspend fun updateScheduleItem(item: ScheduleItem) {
        scheduleDao.update(item.toEntity())
    }

    override suspend fun addDailyScheduleTemplate(template: DailyScheduleTemplate): Long {
        return dailyScheduleTemplateDao.insert(template.toEntity())
    }

    override suspend fun updateDailyScheduleTemplate(template: DailyScheduleTemplate) {
        dailyScheduleTemplateDao.update(template.toEntity())
    }

    override suspend fun deleteDailyScheduleTemplate(id: Long, fromDate: LocalDate) {
        dailyScheduleTemplateDao.deleteById(id)
        scheduleDao.deleteFutureByTemplateId(id, fromDate.toString())
    }

    override suspend fun deleteScheduleItem(id: Long) {
        scheduleDao.deleteById(id)
    }

    override suspend fun addTodoItem(item: TodoItem): Long {
        val sortOrder = if (item.id == 0L) {
            todoDao.getMaxSortOrder(item.date.toString()) + 1
        } else {
            item.sortOrder
        }
        return todoDao.insert(item.copy(sortOrder = sortOrder).toEntity())
    }

    override suspend fun updateTodoItem(item: TodoItem) {
        todoDao.update(item.toEntity())
    }

    override suspend fun reorderTodoItems(ids: List<Long>) {
        todoDao.reorderByIds(ids)
    }

    override suspend fun deleteTodoItem(id: Long) {
        todoDao.deleteById(id)
    }

    override suspend fun toggleItemCompletion(id: Long, type: PlanItemType) {
        when (type) {
            PlanItemType.SCHEDULE -> {
                val current = scheduleDao.getById(id) ?: return
                scheduleDao.update(
                    current.copy(status = current.status.toggleStatusValue())
                )
            }

            PlanItemType.TODO -> {
                val current = todoDao.getById(id) ?: return
                todoDao.update(
                    current.copy(status = current.status.toggleStatusValue())
                )
            }
        }
    }

    private suspend fun ensureDailySchedulesForDate(date: LocalDate) {
        val dateKey = date.toString()
        dailyScheduleTemplateDao.getActiveTemplates().forEach { template ->
            val existing = scheduleDao.getByTemplateAndDate(template.id, dateKey)
            if (existing == null) {
                scheduleDao.insert(
                    ScheduleEntity(
                        date = dateKey,
                        title = template.title,
                        startTime = template.startTime,
                        endTime = template.endTime,
                        note = template.note,
                        status = CompletionStatus.PENDING.name,
                        recurringTemplateId = template.id,
                        reminderEnabled = template.reminderEnabled,
                        reminderMinutesBefore = template.reminderMinutesBefore
                    )
                )
            }
        }
    }
}

private fun ScheduleEntity.toDomain(): ScheduleItem {
    return ScheduleItem(
        id = id,
        date = LocalDate.parse(date),
        title = title,
        startTime = startTime,
        endTime = endTime,
        note = note,
        status = status.toCompletionStatus(),
        recurringTemplateId = recurringTemplateId,
        reminderEnabled = reminderEnabled,
        reminderMinutesBefore = reminderMinutesBefore
    )
}

private fun TodoEntity.toDomain(): TodoItem {
    return TodoItem(
        id = id,
        date = LocalDate.parse(date),
        title = title,
        priority = Priority.fromLevel(priority),
        sortOrder = sortOrder,
        expectTodayCompletion = expectTodayCompletion,
        note = note,
        status = status.toCompletionStatus(),
        dueTime = dueTime,
        reminderEnabled = reminderEnabled,
        reminderMinutesBefore = reminderMinutesBefore
    )
}

private fun ScheduleItem.toEntity(): ScheduleEntity {
    return ScheduleEntity(
        id = id,
        date = date.toString(),
        title = title,
        startTime = startTime,
        endTime = endTime,
        note = note,
        status = status.name,
        recurringTemplateId = recurringTemplateId,
        reminderEnabled = reminderEnabled,
        reminderMinutesBefore = reminderMinutesBefore
    )
}

private fun TodoItem.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        date = date.toString(),
        title = title,
        priority = priority.level,
        sortOrder = sortOrder,
        expectTodayCompletion = expectTodayCompletion,
        note = note,
        status = status.name,
        dueTime = dueTime,
        reminderEnabled = reminderEnabled,
        reminderMinutesBefore = reminderMinutesBefore
    )
}

private fun DailyScheduleTemplate.toEntity(): DailyScheduleTemplateEntity {
    return DailyScheduleTemplateEntity(
        id = id,
        title = title,
        startTime = startTime,
        endTime = endTime,
        note = note,
        reminderEnabled = reminderEnabled,
        reminderMinutesBefore = reminderMinutesBefore,
        active = active
    )
}

private fun String.toCompletionStatus(): CompletionStatus {
    return CompletionStatus.entries.firstOrNull { it.name == this } ?: CompletionStatus.PENDING
}

private fun String.toggleStatusValue(): String {
    return if (this == CompletionStatus.DONE.name) {
        CompletionStatus.PENDING.name
    } else {
        CompletionStatus.DONE.name
    }
}

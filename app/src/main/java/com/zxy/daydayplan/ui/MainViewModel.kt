package com.zxy.daydayplan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zxy.daydayplan.data.repository.UiPreferencesRepository
import com.zxy.daydayplan.domain.model.CompletionStatus
import com.zxy.daydayplan.domain.model.DailyScheduleTemplate
import com.zxy.daydayplan.domain.model.DayPlan
import com.zxy.daydayplan.domain.model.PlanItemType
import com.zxy.daydayplan.domain.model.Priority
import com.zxy.daydayplan.domain.model.ScheduleItem
import com.zxy.daydayplan.domain.model.TodoItem
import com.zxy.daydayplan.domain.repository.DayPlanRepository
import com.zxy.daydayplan.reminder.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: DayPlanRepository,
    private val preferencesRepository: UiPreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val today = LocalDate.now()
    private val dialogState = MutableStateFlow<EditorDialogState>(EditorDialogState.None)
    private val reviewDate = MutableStateFlow(today)

    private val reviewDayPlan = reviewDate.flatMapLatest { date ->
        repository.getDayPlan(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayPlan(today, emptyList(), emptyList())
    )

    val uiState: StateFlow<MainUiState> = combine(
        repository.getDayPlan(today),
        reviewDayPlan,
        preferencesRepository.selectedTab,
        dialogState,
        reviewDate
    ) { dayPlan, reviewPlan, selectedTab, dialog, selectedReviewDate ->
        val now = LocalTime.now()
        MainUiState(
            dayPlan = dayPlan,
            reviewDayPlan = reviewPlan,
            reviewDate = selectedReviewDate,
            selectedTab = selectedTab,
            activeDialog = dialog,
            ongoingSchedule = dayPlan.schedules.firstOrNull { it.isOngoing(now) },
            pendingSchedules = dayPlan.schedules.filter { it.isPendingNowOrLater(now) },
            overdueSchedules = dayPlan.schedules.filter { it.isOverdue(now) },
            pendingTodos = dayPlan.todos.filter { it.status == CompletionStatus.PENDING },
            completedSchedules = dayPlan.schedules.filter { it.status == CompletionStatus.DONE },
            completedTodos = dayPlan.todos.filter { it.status == CompletionStatus.DONE }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(dayPlan = DayPlan(today, emptyList(), emptyList()))
    )

    fun selectTab(tab: AppTab) {
        viewModelScope.launch {
            preferencesRepository.setSelectedTab(tab)
        }
    }

    fun selectReviewDate(date: LocalDate) {
        reviewDate.value = date
    }

    fun openScheduleEditor(item: ScheduleItem? = null) {
        dialogState.value = EditorDialogState.Schedule(item)
    }

    fun openTodoEditor(item: TodoItem? = null) {
        dialogState.value = EditorDialogState.Todo(item)
    }

    fun closeDialog() {
        dialogState.value = EditorDialogState.None
    }

    fun saveSchedule(
        id: Long,
        title: String,
        startTime: String,
        endTime: String,
        note: String,
        repeatDaily: Boolean,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int
    ) {
        viewModelScope.launch {
            val existing = uiState.value.dayPlan.schedules.firstOrNull { it.id == id }
            existing?.let { reminderScheduler.cancelScheduleReminder(it.id) }

            var recurringTemplateId = existing?.recurringTemplateId
            if (repeatDaily) {
                val template = DailyScheduleTemplate(
                    id = recurringTemplateId ?: 0L,
                    title = title.trim(),
                    startTime = startTime,
                    endTime = endTime,
                    note = note.trim(),
                    reminderEnabled = reminderEnabled,
                    reminderMinutesBefore = reminderMinutesBefore
                )
                recurringTemplateId = if (recurringTemplateId == null) {
                    repository.addDailyScheduleTemplate(template)
                } else {
                    repository.updateDailyScheduleTemplate(template.copy(id = recurringTemplateId))
                    recurringTemplateId
                }
                reminderScheduler.scheduleRecurringTemplate(template.copy(id = recurringTemplateId))
            } else if (recurringTemplateId != null) {
                repository.deleteDailyScheduleTemplate(recurringTemplateId, today)
                reminderScheduler.cancelRecurringTemplate(recurringTemplateId)
                recurringTemplateId = null
            }

            val item = ScheduleItem(
                id = id,
                date = today,
                title = title.trim(),
                startTime = startTime,
                endTime = endTime,
                note = note.trim(),
                status = existing?.status ?: CompletionStatus.PENDING,
                recurringTemplateId = recurringTemplateId,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore
            )

            if (id == 0L) {
                repository.addScheduleItem(item)
            } else {
                repository.updateScheduleItem(item)
            }

            if (!repeatDaily) {
                reminderScheduler.scheduleScheduleReminder(item)
            }
            closeDialog()
        }
    }

    fun saveTodo(
        id: Long,
        title: String,
        priority: Priority,
        expectTodayCompletion: Boolean,
        note: String,
        dueTime: String?,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int
    ) {
        viewModelScope.launch {
            val existing = uiState.value.dayPlan.todos.firstOrNull { it.id == id }
            existing?.let { reminderScheduler.cancelTodoReminder(it.id) }
            val item = TodoItem(
                id = id,
                date = today,
                title = title.trim(),
                priority = priority,
                sortOrder = existing?.sortOrder ?: 0,
                expectTodayCompletion = expectTodayCompletion,
                note = note.trim(),
                status = existing?.status ?: CompletionStatus.PENDING,
                dueTime = dueTime,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore
            )
            if (id == 0L) {
                repository.addTodoItem(item)
            } else {
                repository.updateTodoItem(item)
            }
            reminderScheduler.scheduleTodoReminder(item)
            closeDialog()
        }
    }

    fun toggleCompletion(id: Long, type: PlanItemType) {
        viewModelScope.launch {
            repository.toggleItemCompletion(id, type)
            when (type) {
                PlanItemType.SCHEDULE -> {
                    val current = uiState.value.dayPlan.schedules.firstOrNull { it.id == id } ?: return@launch
                    if (current.status == CompletionStatus.PENDING) {
                        reminderScheduler.cancelScheduleReminder(id)
                    } else if (current.recurringTemplateId == null) {
                        reminderScheduler.scheduleScheduleReminder(current.copy(status = CompletionStatus.PENDING))
                    }
                }

                PlanItemType.TODO -> {
                    val current = uiState.value.dayPlan.todos.firstOrNull { it.id == id } ?: return@launch
                    if (current.status == CompletionStatus.PENDING) {
                        reminderScheduler.cancelTodoReminder(id)
                    } else {
                        reminderScheduler.scheduleTodoReminder(current.copy(status = CompletionStatus.PENDING))
                    }
                }
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            val item = uiState.value.dayPlan.schedules.firstOrNull { it.id == id } ?: return@launch
            reminderScheduler.cancelScheduleReminder(id)
            item.recurringTemplateId?.let {
                reminderScheduler.cancelRecurringTemplate(it)
                repository.deleteDailyScheduleTemplate(it, today)
            } ?: repository.deleteScheduleItem(id)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            reminderScheduler.cancelTodoReminder(id)
            repository.deleteTodoItem(id)
        }
    }

    fun moveTodo(id: Long, direction: MoveDirection) {
        val current = uiState.value.pendingTodos.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index == -1) return
        val targetIndex = when (direction) {
            MoveDirection.Up -> (index - 1).takeIf { it >= 0 }
            MoveDirection.Down -> (index + 1).takeIf { it < current.size }
        } ?: return

        val item = current.removeAt(index)
        current.add(targetIndex, item)

        viewModelScope.launch {
            repository.reorderTodoItems(current.map { it.id })
        }
    }

    companion object {
        fun factory(
            repository: DayPlanRepository,
            preferencesRepository: UiPreferencesRepository,
            reminderScheduler: ReminderScheduler
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository, preferencesRepository, reminderScheduler) as T
                }
            }
        }
    }
}

data class MainUiState(
    val dayPlan: DayPlan,
    val reviewDayPlan: DayPlan = DayPlan(LocalDate.now(), emptyList(), emptyList()),
    val reviewDate: LocalDate = LocalDate.now(),
    val selectedTab: AppTab = AppTab.Overview,
    val activeDialog: EditorDialogState = EditorDialogState.None,
    val ongoingSchedule: ScheduleItem? = null,
    val pendingSchedules: List<ScheduleItem> = emptyList(),
    val overdueSchedules: List<ScheduleItem> = emptyList(),
    val pendingTodos: List<TodoItem> = emptyList(),
    val completedSchedules: List<ScheduleItem> = emptyList(),
    val completedTodos: List<TodoItem> = emptyList()
) {
    val completedCount: Int = completedSchedules.size + completedTodos.size
}

sealed interface EditorDialogState {
    data object None : EditorDialogState
    data class Schedule(val item: ScheduleItem?) : EditorDialogState
    data class Todo(val item: TodoItem?) : EditorDialogState
}

enum class MoveDirection {
    Up,
    Down
}

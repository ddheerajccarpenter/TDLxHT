package com.example.data.repository

import com.example.HabitTaskApplication
import com.example.data.local.SubtaskDao
import com.example.data.local.SubtaskEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.local.toDomainModel
import com.example.data.local.toEntity
import com.example.data.model.ReminderOption
import com.example.data.model.RepeatRule
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class TaskRepository(
  private val taskDao: TaskDao,
  private val subtaskDao: SubtaskDao
) {

  val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
    entities.map { it.toDomainModel() }
  }

  val allSubtasks: Flow<List<Subtask>> = subtaskDao.getAllSubtasks().map { entities ->
    entities.map { it.toDomainModel() }
  }

  fun getSubtasksForTask(taskId: String): Flow<List<Subtask>> {
    return subtaskDao.getSubtasksForTask(taskId).map { entities ->
      entities.map { it.toDomainModel() }
    }
  }

  fun getTaskById(taskId: String): Flow<Task?> {
    return taskDao.getTaskById(taskId).map { it?.toDomainModel() }
  }

  suspend fun insertTask(task: Task, subtasks: List<Subtask> = emptyList()) {
    taskDao.insertTask(task.toEntity())
    if (subtasks.isNotEmpty()) {
      val entities = subtasks.map { it.copy(parentTaskId = task.id).toEntity() }
      subtaskDao.insertSubtasks(entities)
    }

    // Schedule reminder if set
    if (task.reminderSetting != ReminderOption.NONE && !task.isCompleted) {
      val minutesBefore = if (task.reminderSetting == ReminderOption.CUSTOM) {
        task.customReminderMinutes
      } else {
        task.reminderSetting.minutesBefore
      }
      ReminderScheduler.scheduleTaskReminder(
        context = HabitTaskApplication.instance,
        taskId = task.id,
        title = task.title,
        dueDate = task.dueDate,
        dueTime = task.dueTime,
        minutesBefore = minutesBefore
      )
    }
  }

  suspend fun updateTask(task: Task, updatedSubtasks: List<Subtask>? = null) {
    val updated = task.copy(updatedTimestamp = System.currentTimeMillis())
    taskDao.updateTask(updated.toEntity())

    if (updatedSubtasks != null) {
      subtaskDao.deleteSubtasksForTask(task.id)
      if (updatedSubtasks.isNotEmpty()) {
        subtaskDao.insertSubtasks(updatedSubtasks.map { it.copy(parentTaskId = task.id).toEntity() })
      }
    }

    // Update reminder: cancel old and schedule new
    ReminderScheduler.cancelTaskReminder(HabitTaskApplication.instance, task.id)
    if (updated.reminderSetting != ReminderOption.NONE && !updated.isCompleted) {
      val minutesBefore = if (updated.reminderSetting == ReminderOption.CUSTOM) {
        updated.customReminderMinutes
      } else {
        updated.reminderSetting.minutesBefore
      }
      ReminderScheduler.scheduleTaskReminder(
        context = HabitTaskApplication.instance,
        taskId = updated.id,
        title = updated.title,
        dueDate = updated.dueDate,
        dueTime = updated.dueTime,
        minutesBefore = minutesBefore
      )
    }
  }

  suspend fun deleteTask(taskId: String) {
    ReminderScheduler.cancelTaskReminder(HabitTaskApplication.instance, taskId)
    taskDao.deleteTaskById(taskId)
    subtaskDao.deleteSubtasksForTask(taskId)
    com.example.data.service.SyncService.deleteCloudTasks(listOf(taskId))
  }

  suspend fun deleteTasksBatch(taskIds: List<String>) {
    if (taskIds.isEmpty()) return
    taskIds.forEach { ReminderScheduler.cancelTaskReminder(HabitTaskApplication.instance, it) }
    taskDao.deleteTasksByIds(taskIds)
    subtaskDao.deleteSubtasksForTasks(taskIds)
    com.example.data.service.SyncService.deleteCloudTasks(taskIds)
  }

  suspend fun toggleTaskCompletion(taskId: String) {
    val taskEntity = taskDao.getTaskByIdSync(taskId) ?: return
    val task = taskEntity.toDomainModel()
    val newCompletedState = !task.isCompleted

    val updatedTask = task.copy(
      isCompleted = newCompletedState,
      updatedTimestamp = System.currentTimeMillis()
    )
    taskDao.updateTask(updatedTask.toEntity())

    if (newCompletedState) {
      // Completed -> cancel reminder
      ReminderScheduler.cancelTaskReminder(HabitTaskApplication.instance, taskId)

      // Handle recurrence if recurring
      if (task.repeatRule != RepeatRule.NONE) {
        val nextDueDate = calculateNextOccurrenceDate(task.dueDate, task.repeatRule, task.repeatDays)
        if (nextDueDate != null) {
          val nextDueDateStr = nextDueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
          val allTasks = taskDao.getAllTasksSync()
          val duplicateExists = allTasks.any {
            it.title.trim().equals(task.title.trim(), ignoreCase = true) &&
              it.dueDate == nextDueDateStr &&
              !it.isCompleted
          }
          if (!duplicateExists) {
            val nextTask = task.copy(
              id = UUID.randomUUID().toString(),
              dueDate = nextDueDateStr,
              isCompleted = false,
              creationTimestamp = System.currentTimeMillis(),
              updatedTimestamp = System.currentTimeMillis()
            )
            // Clone subtasks as uncompleted for the next occurrence
            val existingSubtasks = subtaskDao.getSubtasksForTaskSync(taskId)
            val nextSubtasks = existingSubtasks.map {
              Subtask(
                id = UUID.randomUUID().toString(),
                parentTaskId = nextTask.id,
                title = it.title,
                isCompleted = false,
                creationTimestamp = System.currentTimeMillis()
              )
            }
            insertTask(nextTask, nextSubtasks)
          }
        }
      }
    } else {
      // Uncompleted -> reschedule reminder if needed
      if (task.reminderSetting != ReminderOption.NONE) {
        val minutesBefore = if (task.reminderSetting == ReminderOption.CUSTOM) {
          task.customReminderMinutes
        } else {
          task.reminderSetting.minutesBefore
        }
        ReminderScheduler.scheduleTaskReminder(
          context = HabitTaskApplication.instance,
          taskId = task.id,
          title = task.title,
          dueDate = task.dueDate,
          dueTime = task.dueTime,
          minutesBefore = minutesBefore
        )
      }
    }
  }

  suspend fun insertSubtask(subtask: Subtask) {
    subtaskDao.insertSubtask(subtask.toEntity())
  }

  suspend fun updateSubtask(subtask: Subtask) {
    subtaskDao.updateSubtask(subtask.toEntity())
  }

  suspend fun deleteSubtask(subtaskId: String) {
    subtaskDao.deleteSubtaskById(subtaskId)
  }

  suspend fun toggleSubtaskCompletion(subtaskId: String, autoMarkParent: Boolean = false) {
    val all = subtaskDao.getAllSubtasks()
    // Find subtask from sync query
    // Query directly or update
    val subtasks = subtaskDao.getSubtasksForTaskSync("") // We can update directly
  }

  suspend fun toggleSubtask(subtask: Subtask, autoMarkParent: Boolean = false) {
    val updated = subtask.copy(isCompleted = !subtask.isCompleted)
    subtaskDao.updateSubtask(updated.toEntity())

    if (autoMarkParent) {
      val parentSubtasks = subtaskDao.getSubtasksForTaskSync(subtask.parentTaskId)
      if (parentSubtasks.isNotEmpty()) {
        val allDone = parentSubtasks.all { if (it.id == subtask.id) updated.isCompleted else it.isCompleted }
        if (allDone) {
          val parent = taskDao.getTaskByIdSync(subtask.parentTaskId)
          if (parent != null && !parent.isCompleted) {
            taskDao.updateTask(parent.copy(isCompleted = true, updatedTimestamp = System.currentTimeMillis()))
          }
        }
      }
    }
  }

  companion object {
    fun calculateNextOccurrenceDate(
      currentDateStr: String,
      rule: RepeatRule,
      customDays: List<DayOfWeek>
    ): LocalDate? {
      return try {
        val current = LocalDate.parse(currentDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        when (rule) {
          RepeatRule.NONE -> null
          RepeatRule.DAILY -> current.plusDays(1)
          RepeatRule.WEEKDAYS -> {
            var next = current.plusDays(1)
            while (next.dayOfWeek == DayOfWeek.SATURDAY || next.dayOfWeek == DayOfWeek.SUNDAY) {
              next = next.plusDays(1)
            }
            next
          }
          RepeatRule.WEEKLY -> current.plusWeeks(1)
          RepeatRule.MONTHLY -> current.plusMonths(1)
          RepeatRule.CUSTOM -> {
            if (customDays.isEmpty()) {
              current.plusDays(1)
            } else {
              var next = current.plusDays(1)
              var count = 0
              while (!customDays.contains(next.dayOfWeek) && count < 8) {
                next = next.plusDays(1)
                count++
              }
              next
            }
          }
        }
      } catch (e: Exception) {
        null
      }
    }
  }
}

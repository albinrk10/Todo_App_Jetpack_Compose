package com.albin.todoapp.domain.notifications

import com.albin.todoapp.domain.Task

interface TaskNotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: String)
}

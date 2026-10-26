package com.albin.todoapp.domain.usecase

import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.TaskLocalDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTasksForAiUseCase @Inject constructor(
    private val taskLocalDataSource: TaskLocalDataSource
) {
    suspend operator fun invoke(filter: String? = null): List<Task> {
        val allTasks = taskLocalDataSource.tasksFlow.first()
        if (filter.isNullOrBlank()) return allTasks

        val normalized = filter.trim().lowercase()
        return allTasks.filter {
            it.title.lowercase().contains(normalized) ||
                (it.description?.lowercase()?.contains(normalized) == true)
        }
    }
}

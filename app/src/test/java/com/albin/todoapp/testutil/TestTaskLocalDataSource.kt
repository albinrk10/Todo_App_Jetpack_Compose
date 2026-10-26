package com.albin.todoapp.testutil

import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.TaskLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestTaskLocalDataSource(
    initialTasks: List<Task> = emptyList()
) : TaskLocalDataSource {
    private val _tasksFlow = MutableStateFlow(initialTasks)
    override val tasksFlow: Flow<List<Task>> = _tasksFlow.asStateFlow()

    override suspend fun addTask(task: Task) {
        _tasksFlow.value = _tasksFlow.value + task
    }

    override suspend fun updateTask(updatedTask: Task) {
        _tasksFlow.value = _tasksFlow.value.map {
            if (it.id == updatedTask.id) updatedTask else it
        }
    }

    override suspend fun removeTask(task: Task) {
        _tasksFlow.value = _tasksFlow.value.filterNot { it.id == task.id }
    }

    override suspend fun deleteAllTasks() {
        _tasksFlow.value = emptyList()
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return _tasksFlow.value.find { it.id == taskId }
    }

    override suspend fun removeAllTasks() {
        _tasksFlow.value = emptyList()
    }
}

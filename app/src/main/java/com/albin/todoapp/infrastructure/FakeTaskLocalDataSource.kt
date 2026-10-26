package com.albin.todoapp.infrastructure

import com.albin.todoapp.domain.Category
import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.TaskLocalDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object FakeTaskLocalDataSource : TaskLocalDataSource {
    private val _tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    init {
        _tasksFlow.value = List(10) {
            Task(
                id = it.toString(),
                title = "Tarea $it",
                description = "Descripción de prueba $it",
                category = Category.WORK,
                isCompleted = it % 2 == 0
            )
        }
    }

    override val tasksFlow: Flow<List<Task>>
        get() = _tasksFlow

    override suspend fun addTask(task: Task) {
        val tasks = _tasksFlow.value.toMutableList()
        tasks.add(task)
        delay(100L)
        _tasksFlow.value = tasks
    }

    override suspend fun updateTask(updatedTask: Task) {
        val tasks = _tasksFlow.value.toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == updatedTask.id }
        if (taskIndex != -1) {
            tasks[taskIndex] = updatedTask
            delay(100L)
            _tasksFlow.value = tasks
        }
    }

    override suspend fun removeTask(task: Task) {
        val tasks = _tasksFlow.value.toMutableList()
        tasks.removeIf { it.id == task.id }
        delay(100L)
        _tasksFlow.value = tasks
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

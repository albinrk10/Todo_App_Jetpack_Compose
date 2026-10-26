package com.albin.todoapp.presentation.screens.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.TaskLocalDataSource
import com.albin.todoapp.presentation.navigation.TaskScreenDes
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiEffect
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiIntent
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localDataSource: TaskLocalDataSource
) : ViewModel() {

    var state by mutableStateOf(TaskDetailUiState())
        private set

    private var effectChannel = Channel<TaskDetailUiEffect>()
    val effects = effectChannel.receiveAsFlow()

    // Compatibilidad para observers de event
    val event = effects

    private val canSaveTask = snapshotFlow { state.taskName.text.toString() }
    private val taskData = savedStateHandle.toRoute<TaskScreenDes>()

    private var editedTask: Task? = null

    init {
        taskData.taskId?.let {
            viewModelScope.launch {
                localDataSource.getTaskById(taskData.taskId)?.let { task ->
                    editedTask = task
                    state = state.copy(
                        taskName = TextFieldState(task.title),
                        taskDescription = TextFieldState(task.description ?: ""),
                        isTaskDone = task.isCompleted,
                        category = task.category,
                        date = task.date
                    )
                }
            }
        }

        canSaveTask.onEach {
            state = state.copy(canSaveTask = it.isNotEmpty())
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: TaskDetailUiIntent) {
        viewModelScope.launch {
            when (intent) {
                is TaskDetailUiIntent.ChangeTaskCategory -> {
                    state = state.copy(category = intent.category)
                }
                is TaskDetailUiIntent.ChangeTaskDone -> {
                    state = state.copy(isTaskDone = intent.isTaskDone)
                }
                is TaskDetailUiIntent.ChangeTaskDateTime -> {
                    state = state.copy(date = intent.dateTime)
                }
                is TaskDetailUiIntent.SaveTask -> {
                    val targetDate = state.date ?: LocalDateTime.now().plusHours(1)
                    editedTask?.let {
                        localDataSource.updateTask(
                            updatedTask = it.copy(
                                id = it.id,
                                title = state.taskName.text.toString(),
                                description = state.taskDescription.text.toString(),
                                isCompleted = state.isTaskDone,
                                category = state.category,
                                date = targetDate
                            )
                        )
                    } ?: run {
                        val task = Task(
                            id = UUID.randomUUID().toString(),
                            title = state.taskName.text.toString(),
                            description = state.taskDescription.text.toString(),
                            isCompleted = state.isTaskDone,
                            category = state.category,
                            date = targetDate
                        )
                        localDataSource.addTask(task = task)
                    }
                    effectChannel.send(TaskDetailUiEffect.TaskCreated)
                }
                else -> Unit
            }
        }
    }

    fun onAction(action: TaskDetailUiIntent) = onIntent(action)
}

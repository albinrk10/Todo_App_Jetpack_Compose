package com.albin.todoapp.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albin.todoapp.core.util.DateTimeUtils
import com.albin.todoapp.domain.TaskLocalDataSource
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiEffect
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiIntent
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val taskLocalDataSource: TaskLocalDataSource
) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    private val effectChannel = Channel<HomeUiEffect>()
    val effects = effectChannel.receiveAsFlow()

    // Compatibilidad para observers existentes de events
    val events = effects

    init {
        state = state.copy(
            date = DateTimeUtils.formatCapitalizedFullDate()
        )
        taskLocalDataSource.tasksFlow.onEach { tasks ->
            val completedTasks = tasks
                .filter { it.isCompleted }
                .sortedByDescending { it.date }
            val pendingTasks = tasks
                .filter { !it.isCompleted }
                .sortedByDescending { it.date }

            state = state.copy(
                date = state.date,
                summary = pendingTasks.size.toString(),
                completedTask = completedTasks,
                pendingTask = pendingTasks
            )
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeUiIntent) {
        viewModelScope.launch {
            when (intent) {
                is HomeUiIntent.OnDeleteAllTasks -> {
                    taskLocalDataSource.removeAllTasks()
                    effectChannel.send(HomeUiEffect.AllTaskDeleted)
                }

                is HomeUiIntent.OnDeleteTask -> {
                    taskLocalDataSource.removeTask(intent.task)
                    effectChannel.send(HomeUiEffect.DeletedTask)
                }

                is HomeUiIntent.OnToggleTask -> {
                    val updatedTask = intent.task.copy(isCompleted = !intent.task.isCompleted)
                    taskLocalDataSource.updateTask(updatedTask)
                    effectChannel.send(HomeUiEffect.UpdatedTask)
                }

                else -> Unit
            }
        }
    }

    // Alias para compatibilidad de llamada
    fun onAction(action: HomeUiIntent) = onIntent(action)
}

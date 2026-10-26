package com.albin.todoapp.presentation.screens.home.interactor

import com.albin.todoapp.core.handler.UiIntent
import com.albin.todoapp.domain.Task

sealed interface HomeUiIntent : UiIntent {
    data class OnClickTask(val taskId: String) : HomeUiIntent
    data class OnToggleTask(val task: Task) : HomeUiIntent
    data class OnDeleteTask(val task: Task) : HomeUiIntent
    data object OnDeleteAllTasks : HomeUiIntent
    data object OnAddTask : HomeUiIntent
    data object OnOpenAssistant : HomeUiIntent
}

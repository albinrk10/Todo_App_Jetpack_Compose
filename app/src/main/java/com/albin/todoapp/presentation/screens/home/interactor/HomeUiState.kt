package com.albin.todoapp.presentation.screens.home.interactor

import com.albin.todoapp.core.handler.UiState
import com.albin.todoapp.domain.Task

data class HomeUiState(
    val date: String = "",
    val summary: String = "",
    val completedTask: List<Task> = emptyList(),
    val pendingTask: List<Task> = emptyList()
) : UiState

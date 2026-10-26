package com.albin.todoapp.presentation.screens.detail.interactor

import androidx.compose.foundation.text.input.TextFieldState
import com.albin.todoapp.core.handler.UiState
import com.albin.todoapp.domain.Category
import java.time.LocalDateTime

data class TaskDetailUiState(
    val taskName: TextFieldState = TextFieldState(),
    val taskDescription: TextFieldState = TextFieldState(),
    val isTaskDone: Boolean = false,
    val category: Category? = null,
    val date: LocalDateTime? = LocalDateTime.now().plusHours(1),
    val canSaveTask: Boolean = false
) : UiState

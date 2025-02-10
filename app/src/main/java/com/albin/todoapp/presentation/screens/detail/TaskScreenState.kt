package com.albin.todoapp.presentation.screens.detail
import androidx.compose.foundation.text.input.TextFieldState
import com.albin.todoapp.domain.Category

data class TaskScreenState(
    val taskName:TextFieldState = TextFieldState(),
    val taskDescription: TextFieldState = TextFieldState(),
    val isTaskDone: Boolean = false,
    val category: Category? = null,
    val canSaveTask: Boolean = false
)
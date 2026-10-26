package com.albin.todoapp.presentation.screens.detail.interactor

import com.albin.todoapp.core.handler.UiIntent
import com.albin.todoapp.domain.Category
import java.time.LocalDateTime

sealed interface TaskDetailUiIntent : UiIntent {
    data object SaveTask : TaskDetailUiIntent
    data object Back : TaskDetailUiIntent
    data class ChangeTaskCategory(val category: Category?) : TaskDetailUiIntent
    data class ChangeTaskDone(val isTaskDone: Boolean) : TaskDetailUiIntent
    data class ChangeTaskDateTime(val dateTime: LocalDateTime?) : TaskDetailUiIntent
}

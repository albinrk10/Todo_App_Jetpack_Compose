package com.albin.todoapp.presentation.screens.detail.interactor

import com.albin.todoapp.core.handler.UiEffect

sealed interface TaskDetailUiEffect : UiEffect {
    data object TaskCreated : TaskDetailUiEffect
}

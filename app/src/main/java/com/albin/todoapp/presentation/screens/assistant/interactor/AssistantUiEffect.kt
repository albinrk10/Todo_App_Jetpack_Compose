package com.albin.todoapp.presentation.screens.assistant.interactor

import com.albin.todoapp.core.handler.UiEffect

sealed interface AssistantUiEffect : UiEffect {
    data class ShowToast(val message: String) : AssistantUiEffect
    data object ScrollToBottom : AssistantUiEffect
}

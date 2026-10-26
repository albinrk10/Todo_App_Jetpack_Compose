package com.albin.todoapp.presentation.screens.assistant.interactor

import com.albin.todoapp.core.mvi.UiState
import com.albin.todoapp.domain.ai.AiMessage
import com.albin.todoapp.domain.ai.ModelState

data class AssistantUiState(
    val inputText: String = "",
    val isListening: Boolean = false,
    val messages: List<AiMessage> = emptyList(),
    val modelState: ModelState = ModelState.NotDownloaded
) : UiState

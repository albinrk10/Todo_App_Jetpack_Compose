package com.albin.todoapp.presentation.screens.assistant.interactor

import com.albin.todoapp.core.handler.UiIntent

sealed interface AssistantUiIntent : UiIntent {
    data class ChangeInputText(val text: String) : AssistantUiIntent
    data object SendMessage : AssistantUiIntent
    data class SendPrompt(val prompt: String) : AssistantUiIntent
    data class VoiceResult(val text: String) : AssistantUiIntent
    data object ClearHistory : AssistantUiIntent
    data object DownloadModel : AssistantUiIntent
}

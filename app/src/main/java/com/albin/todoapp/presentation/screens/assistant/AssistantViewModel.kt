package com.albin.todoapp.presentation.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albin.todoapp.domain.ai.AiAgentRepository
import com.albin.todoapp.domain.ai.AiMessage
import com.albin.todoapp.domain.ai.ModelState
import com.albin.todoapp.presentation.screens.assistant.interactor.AssistantUiEffect
import com.albin.todoapp.presentation.screens.assistant.interactor.AssistantUiIntent
import com.albin.todoapp.presentation.screens.assistant.interactor.AssistantUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AiAgentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<AiMessage>> = repository.messages
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val modelState: StateFlow<ModelState> = repository.modelState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ModelState.NotDownloaded)

    private val effectChannel = Channel<AssistantUiEffect>()
    val effects = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.messages.collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
        viewModelScope.launch {
            repository.modelState.collect { model ->
                _uiState.update { it.copy(modelState = model) }
            }
        }
    }

    fun onIntent(intent: AssistantUiIntent) {
        when (intent) {
            is AssistantUiIntent.ChangeInputText -> {
                _uiState.update { it.copy(inputText = intent.text) }
            }
            is AssistantUiIntent.SendMessage -> {
                val text = _uiState.value.inputText.trim()
                if (text.isNotEmpty()) {
                    _uiState.update { it.copy(inputText = "") }
                    viewModelScope.launch {
                        repository.processUserPrompt(text)
                    }
                }
            }
            is AssistantUiIntent.SendPrompt -> {
                if (intent.prompt.isNotBlank()) {
                    viewModelScope.launch {
                        repository.processUserPrompt(intent.prompt.trim())
                    }
                }
            }
            is AssistantUiIntent.VoiceResult -> {
                val text = intent.text.trim()
                if (text.isNotEmpty()) {
                    _uiState.update { it.copy(inputText = "") }
                    viewModelScope.launch {
                        repository.processUserPrompt(text)
                    }
                }
            }
            is AssistantUiIntent.ClearHistory -> {
                viewModelScope.launch {
                    repository.clearHistory()
                }
            }
            is AssistantUiIntent.DownloadModel -> {
                viewModelScope.launch {
                    repository.downloadModel()
                }
            }
        }
    }

    // Funciones helper para delegar a onIntent
    fun onInputTextChanged(text: String) = onIntent(AssistantUiIntent.ChangeInputText(text))
    fun onSendMessage() = onIntent(AssistantUiIntent.SendMessage)
    fun onSendPrompt(prompt: String) = onIntent(AssistantUiIntent.SendPrompt(prompt))
    fun onVoiceResult(transcribedText: String) = onIntent(AssistantUiIntent.VoiceResult(transcribedText))
    fun onDownloadModel() = onIntent(AssistantUiIntent.DownloadModel)
    fun onClearHistory() = onIntent(AssistantUiIntent.ClearHistory)
}

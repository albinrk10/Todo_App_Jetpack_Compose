package com.albin.todoapp.domain.ai

import kotlinx.coroutines.flow.StateFlow

interface AiAgentRepository {
    val modelState: StateFlow<ModelState>
    val messages: StateFlow<List<AiMessage>>

    suspend fun downloadModel()
    suspend fun processUserPrompt(prompt: String)
    suspend fun clearHistory()
}

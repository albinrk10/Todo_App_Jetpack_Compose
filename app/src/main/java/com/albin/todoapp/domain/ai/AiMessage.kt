package com.albin.todoapp.domain.ai

import java.time.LocalDateTime
import java.util.UUID

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val toolCall: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isPending: Boolean = false
)

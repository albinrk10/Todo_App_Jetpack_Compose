package com.albin.todoapp.infrastructure.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLlmEngine @Inject constructor() {
    private val mutex = Mutex()
    private var llmInference: LlmInference? = null

    val isLoaded: Boolean
        get() = llmInference != null

    suspend fun load(context: Context, modelPath: String): Result<Unit> = mutex.withLock {
        if (llmInference != null) return Result.success(Unit)
        withContext(Dispatchers.Default) {
            runCatching {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .setMaxTokens(1024)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            }
        }
    }

    suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        val inference = llmInference
            ?: return@withContext Result.failure(IllegalStateException("El modelo no está cargado."))
        runCatching {
            inference.generateResponse(prompt)
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}

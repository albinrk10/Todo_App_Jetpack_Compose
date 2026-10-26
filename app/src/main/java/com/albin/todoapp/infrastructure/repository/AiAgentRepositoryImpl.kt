package com.albin.todoapp.infrastructure.repository

import android.content.Context
import com.albin.todoapp.domain.ai.AiAgentRepository
import com.albin.todoapp.domain.ai.AiMessage
import com.albin.todoapp.domain.ai.ModelState
import com.albin.todoapp.infrastructure.ai.AgentEvent
import com.albin.todoapp.infrastructure.ai.LocalLlmEngine
import com.albin.todoapp.infrastructure.ai.LocalModelConfig
import com.albin.todoapp.infrastructure.ai.ModelDownloader
import com.albin.todoapp.infrastructure.ai.TodoAiAgent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAgentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloader: ModelDownloader,
    private val engine: LocalLlmEngine,
    private val agent: TodoAiAgent
) : AiAgentRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _modelState = MutableStateFlow<ModelState>(ModelState.Ready)
    override val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private val initialWelcome = AiMessage(
        text = "¡Hola! Soy tu asistente de tareas. Puedes pedirme crear recordatorios con fecha y hora (ej: 'Comprar leche hoy a las 7:00 pm') o preguntarme qué tareas tienes pendientes.",
        isUser = false
    )

    private val _messages = MutableStateFlow<List<AiMessage>>(listOf(initialWelcome))
    override val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val modelFile: File
        get() = File(context.noBackupFilesDir, "models/${LocalModelConfig.FILE_NAME}")

    init {
        checkModelStatus()
    }

    private fun checkModelStatus() {
        if (modelFile.exists()) {
            if (modelFile.length() < 100_000_000L) {
                // Limpiar archivo corrupto o parcial si existía
                modelFile.delete()
            }
        }
        _modelState.value = ModelState.Ready
    }

    override suspend fun downloadModel() {
        if (_modelState.value is ModelState.Downloading) {
            return
        }
        _modelState.value = ModelState.Downloading(0f)

        scope.launch {
            modelDownloader.download(
                urlStr = LocalModelConfig.DOWNLOAD_URL,
                targetFile = modelFile,
                expectedSizeBytes = LocalModelConfig.EXPECTED_SIZE_BYTES
            ).catch {
                if (modelFile.exists() && modelFile.length() < 100_000_000L) {
                    modelFile.delete()
                }
                _modelState.value = ModelState.Ready
            }.collect { progress ->
                _modelState.value = ModelState.Downloading(progress.fraction)
                if (progress.fraction >= 1.0f) {
                    _modelState.value = ModelState.Ready
                }
            }
        }
    }

    override suspend fun processUserPrompt(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        val userMessage = AiMessage(text = cleanPrompt, isUser = true)
        val pendingAssistantMessage = AiMessage(
            text = "Analizando tu recordatorio...",
            isUser = false,
            isPending = true
        )

        _messages.value = _messages.value + userMessage + pendingAssistantMessage

        scope.launch {
            var finalAnswer = "No pude procesar la solicitud."
            var executedTool: String? = null
            try {
                agent.process(cleanPrompt).collect { event ->
                    when (event) {
                        is AgentEvent.ToolExecuting -> {
                            executedTool = "${event.toolName} · ${event.engineName}"
                            updatePendingMessage("Ejecutando acción (${event.engineName})...", executedTool)
                        }
                        is AgentEvent.Completed -> {
                            finalAnswer = event.responseText
                        }
                    }
                }
            } catch (e: Exception) {
                finalAnswer = "Ocurrió un error al procesar tu solicitud: ${e.message}"
            } finally {
                replacePendingMessage(finalAnswer, executedTool)
            }
        }
    }

    private fun updatePendingMessage(newText: String, toolCall: String?) {
        _messages.value = _messages.value.map { msg ->
            if (msg.isPending) msg.copy(text = newText, toolCall = toolCall) else msg
        }
    }

    private fun replacePendingMessage(finalText: String, toolCall: String?) {
        _messages.value = _messages.value.map { msg ->
            if (msg.isPending) msg.copy(text = finalText, toolCall = toolCall, isPending = false) else msg
        }
    }

    override suspend fun clearHistory() {
        _messages.value = listOf(initialWelcome)
    }
}

package com.albin.todoapp.infrastructure.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AgentEvent {
    data class ToolExecuting(
        val toolName: String,
        val arguments: String = "",
        val engineName: String = "Motor Local NLP"
    ) : AgentEvent
    data class Completed(val responseText: String) : AgentEvent
}

@Singleton
class TodoAiAgent @Inject constructor(
    private val engine: LocalLlmEngine,
    private val createTaskTool: CreateTaskTool,
    private val getTasksTool: GetTasksTool,
    private val searchTasksTool: SearchTasksTool,
    private val setTaskDoneTool: SetTaskDoneTool
) {
    val isLlmLoaded: Boolean
        get() = engine.isLoaded

    fun process(prompt: String): Flow<AgentEvent> = flow {
        // 0. Inteligencia conversacional humana (ok, gracias, saludos, ayuda)
        val conversationalReply = ConversationalReplyHandler.checkConversational(prompt)
        if (conversationalReply != null) {
            emit(AgentEvent.Completed(conversationalReply))
            return@flow
        }

        if (engine.isLoaded) {
            val formattedPrompt = buildLlmPrompt(prompt)
            val result = engine.generateResponse(formattedPrompt)
            if (result.isSuccess) {
                val output = result.getOrThrow()
                val parsedReply = handleLlmOutput(output) { toolName, args, engineName ->
                    emit(AgentEvent.ToolExecuting(toolName, args, engineName))
                }
                emit(AgentEvent.Completed(parsedReply))
                return@flow
            }
        }

        // Fast local fallback (instant mode / intelligent rule & NLP agent)
        val fallbackReply = processLocalFallback(prompt) { toolName, args, engineName ->
            emit(AgentEvent.ToolExecuting(toolName, args, engineName))
        }
        emit(AgentEvent.Completed(fallbackReply))
    }

    private suspend fun handleLlmOutput(
        output: String,
        onToolExecute: suspend (String, String, String) -> Unit
    ): String {
        val lower = output.lowercase()
        return if (lower.contains("create_task") || lower.contains("\"action\":\"create\"")) {
            val cleanTitle = TaskTitleCleaner.clean(output)
            onToolExecute(createTaskTool.name, cleanTitle, "Gemma 2B LLM")
            val result = createTaskTool.execute(mapOf("title" to cleanTitle))
            val title = result["title"]?.toString() ?: cleanTitle
            "¡Listo! He agendado tu recordatorio: \"$title\"."
        } else if (lower.contains("get_tasks") || lower.contains("\"action\":\"list\"")) {
            onToolExecute(getTasksTool.name, "todas", "Gemma 2B LLM")
            val result = getTasksTool.execute(emptyMap())
            @Suppress("UNCHECKED_CAST")
            val tasks = result["tasks"] as? List<Map<String, Any?>> ?: emptyList()
            if (tasks.isEmpty()) {
                "No tienes tareas pendientes."
            } else {
                val listText = tasks.take(5).joinToString("\n") { "• ${it["title"]}" }
                "Tienes ${tasks.size} tarea(s):\n$listText"
            }
        } else {
            output.trim().ifEmpty { "Acción completada con éxito." }
        }
    }

    private suspend fun processLocalFallback(
        prompt: String,
        onToolExecute: suspend (String, String, String) -> Unit
    ): String {
        val lower = prompt.lowercase().trim()

        // 1. Marcar tarea como completada
        if (lower.startsWith("completar ") || lower.startsWith("marcar ") || lower.startsWith("ya hice ") || lower.startsWith("ya compré ")) {
            val taskQuery = lower
                .removePrefix("completar ")
                .removePrefix("marcar ")
                .removePrefix("como hecha ")
                .removePrefix("como completada ")
                .removePrefix("ya hice ")
                .removePrefix("ya compré ")
                .trim()
            onToolExecute(setTaskDoneTool.name, taskQuery, "Motor Local NLP")
            val result = setTaskDoneTool.execute(mapOf("title" to taskQuery, "done" to true))
            return if (result["status"] == "success") {
                "¡Excelente! He marcado como completada la tarea: \"${result["title"]}\"."
            } else {
                result["message"]?.toString() ?: "No encontré la tarea para marcar como completada."
            }
        }

        // 2. Consulta de tareas pendientes o existentes
        val isQuery = lower.contains("qué tareas") ||
            lower.contains("que tareas") ||
            lower.contains("mis tareas") ||
            lower.contains("tengo algo") ||
            lower.contains("listar") ||
            lower.contains("mostrar") ||
            lower.contains("pendientes")

        if (isQuery) {
            onToolExecute(getTasksTool.name, "pendientes", "Motor Local NLP")
            val result = getTasksTool.execute(emptyMap())
            @Suppress("UNCHECKED_CAST")
            val tasks = result["tasks"] as? List<Map<String, Any?>> ?: emptyList()
            if (tasks.isEmpty()) {
                return "No tienes ninguna tarea registrada por el momento. ¡Estás al día!"
            }
            val pending = tasks.filter { (it["isCompleted"] as? Boolean) == false }
            val count = pending.size
            if (count == 0) {
                return "¡No tienes ninguna tarea pendiente! Todas tus tareas están completadas."
            }
            val taskListText = pending.take(6).joinToString("\n") { "• ${it["title"]}" }
            return "Tienes $count tarea(s) pendiente(s):\n$taskListText"
        }

        // 3. Buscar tareas por palabra clave
        if (lower.startsWith("buscar ") || lower.startsWith("busca ")) {
            val query = lower.removePrefix("buscar ").removePrefix("busca ").trim()
            onToolExecute(searchTasksTool.name, query, "Motor Local NLP")
            val result = searchTasksTool.execute(mapOf("query" to query))
            @Suppress("UNCHECKED_CAST")
            val tasks = result["tasks"] as? List<Map<String, Any?>> ?: emptyList()
            return if (tasks.isEmpty()) {
                "No encontré ninguna tarea que contenga \"$query\"."
            } else {
                val listText = tasks.joinToString("\n") { "• ${it["title"]}" }
                "Encontré ${tasks.size} tarea(s) sobre \"$query\":\n$listText"
            }
        }

        // 4. Creación inteligente de tarea con horarios en lenguaje natural y limpieza de título
        val category = extractCategory(prompt)
        val cleanTitle = TaskTitleCleaner.clean(prompt, category)
        val parsedDateTime = DateExpressions.parseDateTime(prompt)
        val spokenTime = DateExpressions.formatSpokenDateTime(parsedDateTime)

        onToolExecute(createTaskTool.name, cleanTitle, "Motor Local NLP")

        val result = createTaskTool.execute(
            mapOf(
                "title" to cleanTitle,
                "due_date" to parsedDateTime.toString(),
                "category" to category
            )
        )

        val title = result["title"]?.toString() ?: cleanTitle
        val description = result["description"]?.toString() ?: ""
        val categoryDisplay = when (result["category"]?.toString()) {
            "WORK" -> "Trabajo"
            "PERSONAL" -> "Personal"
            "SHOPPING" -> "Compras"
            else -> "General"
        }

        return """
            ¡Listo! He programado tu recordatorio:
            • Tarea: "$title"
            • Aviso: $spokenTime
            • Categoría: $categoryDisplay
            • Detalle: $description

            Te notificaré con sonido y vibración exactamente a esa hora.
        """.trimIndent()
    }

    private fun extractCategory(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("comprar") || lower.contains("super") || lower.contains("mercado") || lower.contains("pan") || lower.contains("leche") -> "SHOPPING"
            lower.contains("trabajo") || lower.contains("reunion") || lower.contains("reunión") || lower.contains("informe") || lower.contains("reporte") || lower.contains("proyecto") -> "WORK"
            lower.contains("casa") || lower.contains("médico") || lower.contains("medico") || lower.contains("llamar") || lower.contains("dentista") -> "PERSONAL"
            else -> "OTHER"
        }
    }

    private fun buildLlmPrompt(userInput: String): String {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("es", "ES")))
        return """
            Instrucción: Eres un asistente para una app de tareas. Hoy es $today.
            Si el usuario quiere crear una tarea, responde exactamente:
            create_task: [título de la tarea]
            Si el usuario pregunta por sus tareas, responde:
            get_tasks
            Usuario: $userInput
            Respuesta:
        """.trimIndent()
    }
}

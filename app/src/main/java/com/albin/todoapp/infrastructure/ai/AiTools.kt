package com.albin.todoapp.infrastructure.ai

import com.albin.todoapp.domain.TaskLocalDataSource
import com.albin.todoapp.domain.usecase.CreateTaskFromAiUseCase
import com.albin.todoapp.domain.usecase.GetTasksForAiUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateTaskTool @Inject constructor(
    private val createTaskFromAiUseCase: CreateTaskFromAiUseCase
) : AgentTool {
    override val name: String = "create_task"
    override val description: String =
        "Crea un nuevo recordatorio o tarea en la base de datos de tareas. Úsalo cuando el usuario pida agregar, recordar, agendar o crear una tarea."

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "title",
            description = "El título o acción de la tarea (ej: 'Comprar leche', 'Reunión de equipo').",
            type = ParameterType.STRING,
            isRequired = true
        ),
        ToolParameter(
            name = "description",
            description = "Detalles adicionales opcionales.",
            type = ParameterType.STRING,
            isRequired = false
        ),
        ToolParameter(
            name = "due_date",
            description = "Fecha u hora de vencimiento en formato natural (ej: 'mañana a las 5pm', 'hoy a las 7:00 pm').",
            type = ParameterType.STRING,
            isRequired = false
        ),
        ToolParameter(
            name = "category",
            description = "Categoría de la tarea: WORK, PERSONAL, SHOPPING, u OTHER.",
            type = ParameterType.STRING,
            isRequired = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): Map<String, Any?> {
        val title = arguments["title"]?.toString() ?: "Nueva tarea"
        val description = arguments["description"]?.toString()
        val dueDate = arguments["due_date"]?.toString()
        val category = arguments["category"]?.toString()

        val task = createTaskFromAiUseCase(
            title = title,
            description = description,
            categoryName = category,
            dueDate = dueDate
        )

        return mapOf(
            "status" to "success",
            "message" to "Tarea creada con éxito",
            "taskId" to task.id,
            "title" to task.title,
            "description" to (task.description ?: ""),
            "category" to (task.category?.name ?: "OTHER"),
            "date" to task.date.toString()
        )
    }
}

class GetTasksTool @Inject constructor(
    private val getTasksForAiUseCase: GetTasksForAiUseCase
) : AgentTool {
    override val name: String = "get_tasks"
    override val description: String =
        "Consulta las tareas existentes del usuario. Úsalo cuando el usuario pregunte qué tareas tiene, si tiene algo pendiente o qué debe hacer."

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "filter",
            description = "Palabra clave opcional para filtrar tareas (ej: 'compras', 'trabajo').",
            type = ParameterType.STRING,
            isRequired = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): Map<String, Any?> {
        val filter = arguments["filter"]?.toString()
        val tasks = getTasksForAiUseCase(filter)
        val taskSummaries = tasks.map {
            mapOf(
                "title" to it.title,
                "description" to (it.description ?: ""),
                "isCompleted" to it.isCompleted,
                "category" to (it.category?.name ?: "OTHER"),
                "date" to it.date.toString()
            )
        }
        return mapOf(
            "status" to "success",
            "count" to tasks.size,
            "tasks" to taskSummaries
        )
    }
}

class SearchTasksTool @Inject constructor(
    private val getTasksForAiUseCase: GetTasksForAiUseCase
) : AgentTool {
    override val name: String = "search_tasks"
    override val description: String =
        "Busca tareas por coincidencia de palabras clave en el título o descripción."

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "query",
            description = "Palabra clave a buscar (ej: 'leche', 'reunión').",
            type = ParameterType.STRING,
            isRequired = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): Map<String, Any?> {
        val query = arguments["query"]?.toString() ?: ""
        val tasks = getTasksForAiUseCase(query)
        return mapOf(
            "query" to query,
            "count" to tasks.size,
            "tasks" to tasks.map {
                mapOf(
                    "title" to it.title,
                    "isCompleted" to it.isCompleted,
                    "date" to it.date.toString()
                )
            }
        )
    }
}

class SetTaskDoneTool @Inject constructor(
    private val taskLocalDataSource: TaskLocalDataSource
) : AgentTool {
    override val name: String = "set_task_done"
    override val description: String =
        "Marca una tarea como completada o pendiente buscando por parte de su título."

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "title",
            description = "Título o palabra clave de la tarea a marcar.",
            type = ParameterType.STRING,
            isRequired = true
        ),
        ToolParameter(
            name = "done",
            description = "true para marcar como hecha, false para marcar como pendiente.",
            type = ParameterType.BOOLEAN,
            isRequired = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): Map<String, Any?> {
        val titleQuery = arguments["title"]?.toString()?.lowercase() ?: ""
        val isDone = (arguments["done"] as? Boolean) ?: true

        val allTasks = taskLocalDataSource.tasksFlow.first()
        val matched = allTasks.find { it.title.lowercase().contains(titleQuery) }

        return if (matched != null) {
            val updated = matched.copy(isCompleted = isDone)
            taskLocalDataSource.updateTask(updated)
            mapOf(
                "status" to "success",
                "message" to "Tarea actualizada con éxito.",
                "title" to updated.title,
                "isCompleted" to updated.isCompleted
            )
        } else {
            mapOf(
                "status" to "not_found",
                "message" to "No se encontró ninguna tarea que coincida con '$titleQuery'."
            )
        }
    }
}

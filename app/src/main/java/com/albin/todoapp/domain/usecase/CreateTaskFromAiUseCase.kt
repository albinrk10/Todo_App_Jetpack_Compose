package com.albin.todoapp.domain.usecase

import com.albin.todoapp.infrastructure.ai.DateExpressions
import com.albin.todoapp.domain.Category
import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.TaskLocalDataSource
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class CreateTaskFromAiUseCase @Inject constructor(
    private val taskLocalDataSource: TaskLocalDataSource
) {
    suspend operator fun invoke(
        title: String,
        description: String? = null,
        categoryName: String? = null,
        dueDate: String? = null
    ): Task {
        val cleanTitle = title.trim().ifEmpty { "Nueva tarea" }
        val category = resolveCategory(categoryName, cleanTitle)
        val date = if (dueDate.isNullOrBlank()) {
            LocalDateTime.now().plusHours(1)
        } else {
            runCatching { LocalDateTime.parse(dueDate) }.getOrNull()
                ?: DateExpressions.parseDateTime(dueDate)
        }

        val generatedDescription = description?.trim()?.ifEmpty { null }
            ?: generateContextualDescription(cleanTitle, category)

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            description = generatedDescription,
            isCompleted = false,
            category = category,
            date = date
        )

        taskLocalDataSource.addTask(task)
        return task
    }

    private fun resolveCategory(categoryName: String?, title: String): Category {
        if (!categoryName.isNullOrBlank()) {
            val normalized = categoryName.uppercase().trim()
            Category.entries.find { it.name == normalized }?.let { return it }
        }

        val lower = title.lowercase()
        return when {
            lower.contains("comprar") || lower.contains("super") || lower.contains("mercado") || lower.contains("tienda") || lower.contains("pan") || lower.contains("leche") -> Category.SHOPPING
            lower.contains("trabajo") || lower.contains("reunion") || lower.contains("reunión") || lower.contains("informe") || lower.contains("proyecto") || lower.contains("reporte") || lower.contains("entregar") -> Category.WORK
            lower.contains("personal") || lower.contains("casa") || lower.contains("llamar") || lower.contains("medico") || lower.contains("médico") || lower.contains("dentista") || lower.contains("ejercicio") -> Category.PERSONAL
            else -> Category.OTHER
        }
    }

    private fun generateContextualDescription(title: String, category: Category): String {
        val lower = title.lowercase()
        return when {
            lower.contains("comprar") || lower.contains("super") || lower.contains("mercado") || lower.contains("leche") || lower.contains("pan") ->
                "Recordatorio para realizar compras pendientes en el supermercado o tienda."
            lower.contains("reunion") || lower.contains("reunión") ->
                "Preparar puntos a tratar y objetivos clave para la reunión."
            lower.contains("pagar") || lower.contains("recibo") || lower.contains("factura") ->
                "Efectuar el pago pendiente antes de la fecha de vencimiento."
            lower.contains("medico") || lower.contains("médico") || lower.contains("doctor") || lower.contains("dentista") ->
                "Consulta y cita médica programada."
            lower.contains("llamar") || lower.contains("contactar") ->
                "Llamada telefónica pendiente para coordinar detalles."
            lower.contains("estudiar") || lower.contains("examen") || lower.contains("repasar") ->
                "Sesión de estudio y revisión de material pendiente."
            lower.contains("ejercicio") || lower.contains("entrenar") || lower.contains("gimnasio") ->
                "Rutina de entrenamiento físico y bienestar."
            category == Category.WORK ->
                "Tarea laboral programada para avanzar con el flujo de trabajo."
            category == Category.SHOPPING ->
                "Artículo o insumo pendiente por adquirir."
            category == Category.PERSONAL ->
                "Actividad personal programada para seguimiento."
            else ->
                "Recordatorio organizado por tu Asistente IA."
        }
    }
}

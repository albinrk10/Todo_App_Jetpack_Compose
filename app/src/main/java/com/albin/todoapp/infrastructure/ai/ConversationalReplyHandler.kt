package com.albin.todoapp.infrastructure.ai

import java.util.Locale

/**
 * Filtro inteligente de intenciones conversacionales humanas.
 * Detecta frases como "ok", "gracias", "hola", "dale", "ayuda", etc.,
 * y devuelve respuestas amigables sin crear tareas ni programar falsas alarmas.
 */
object ConversationalReplyHandler {

    private val ACKNOWLEDGMENTS = setOf(
        "ok", "okay", "oki", "okey", "oka", "vale", "dale", "listo", "entendido",
        "perfecto", "bueno", "bien", "genial", "excelente", "chevere", "chévere",
        "bacán", "bacan", "ya veo", "de acuerdo", "ta bien", "está bien", "esta bien",
        "gracias", "muchas gracias", "grx", "mil gracias", "gracias asistente"
    )

    private val GREETINGS = setOf(
        "hola", "buenas", "buenos días", "buenos dias", "buenas tardes", "buenas noches",
        "qué tal", "que tal", "cómo estás", "como estas", "hola asistente", "hey"
    )

    private val FAREWELLS = setOf(
        "adiós", "adios", "chao", "chau", "hasta luego", "nos vemos", "bye", "hasta pronto"
    )

    private val HELP = setOf(
        "ayuda", "qué puedes hacer", "que puedes hacer", "cómo funciona", "como funciona",
        "instrucciones", "comandos", "opciones"
    )

    fun checkConversational(prompt: String): String? {
        val clean = prompt.trim().lowercase(Locale("es", "ES"))
            .replace(Regex("""[¡!¿?.,;:]"""), "")
            .trim()

        if (clean in ACKNOWLEDGMENTS) {
            return "¡Con gusto! Aquí estoy si necesitas programar otra tarea o consultar tus pendientes."
        }

        if (clean in GREETINGS) {
            return "¡Hola! ¿En qué te ayudo hoy? Puedes dictarme una tarea con su hora (ej: 'Comprar pan hoy a las 8 pm') o preguntarme qué pendientes tienes."
        }

        if (clean in FAREWELLS) {
            return "¡Hasta luego! Recuerda que puedes abrir el asistente en cualquier momento para agendar tus tareas."
        }

        if (clean in HELP) {
            return """
                Puedo ayudarte con tus tareas de forma rápida y 100% offline:
                • ⏰ Crear tareas con alarma (ej: "Comprar pan hoy a las 8 pm")
                • 📋 Ver tus tareas pendientes (ej: "¿Qué tareas tengo?")
                • 🔍 Buscar tareas existentes (ej: "Buscar informe")
                • ✅ Marcar como completada (ej: "Ya compré el pan")
            """.trimIndent()
        }

        return null
    }
}

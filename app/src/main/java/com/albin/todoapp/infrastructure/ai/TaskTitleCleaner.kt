package com.albin.todoapp.infrastructure.ai

import java.util.Locale

/**
 * Limpiador inteligente de títulos de tareas a partir de lenguaje natural y dictado por voz.
 * Elimina frases introductorias coloquiales ("hazme acordar que quiero", "tengo que"),
 * extrae y remueve expresiones temporales ("para hoy en la tarde aprox 8 pm", "que las 4:21"),
 * corrige errores comunes de transcripción fonética ("te go que" -> "tengo que", "hermnaos" -> "hermanos"),
 * normaliza jerga ("mana" -> "hermana", "tbm" -> "también"),
 * y antepone verbos de acción pertinentes (ej: "Comprar pan para mis hermanos").
 */
object TaskTitleCleaner {

    private val SLANG_REPLACEMENTS = mapOf(
        Regex("""\bte\s+go\s+que\b""", RegexOption.IGNORE_CASE) to "tengo que",
        Regex("""\bhermnaos\b""", RegexOption.IGNORE_CASE) to "hermanos",
        Regex("""\bhermnao\b""", RegexOption.IGNORE_CASE) to "hermano",
        Regex("""\bhermna\b""", RegexOption.IGNORE_CASE) to "hermana",
        Regex("""\bhermnas\b""", RegexOption.IGNORE_CASE) to "hermanas",
        Regex("""\bmana\b""", RegexOption.IGNORE_CASE) to "hermana",
        Regex("""\bmanita\b""", RegexOption.IGNORE_CASE) to "hermanita",
        Regex("""\bmano\b""", RegexOption.IGNORE_CASE) to "hermano",
        Regex("""\bmanito\b""", RegexOption.IGNORE_CASE) to "hermanito",
        Regex("""\btbm\b""", RegexOption.IGNORE_CASE) to "también",
        Regex("""\btb\b""", RegexOption.IGNORE_CASE) to "también",
        Regex("""\bpa\b""", RegexOption.IGNORE_CASE) to "para",
        Regex("""\bpa'\b""", RegexOption.IGNORE_CASE) to "para",
        Regex("""\bk\b""", RegexOption.IGNORE_CASE) to "que",
        Regex("""\bq\b""", RegexOption.IGNORE_CASE) to "que",
        Regex("""\bxq\b""", RegexOption.IGNORE_CASE) to "porque",
        Regex("""\bd\b""", RegexOption.IGNORE_CASE) to "de"
    )

    private val PREFIXES = listOf(
        "hasme acordar que quiero",
        "hazme acordar que quiero",
        "hasme acordar que",
        "hazme acordar que",
        "hasme acordar",
        "hazme acordar",
        "hasme acuerdo que",
        "hazme acuerdo de que",
        "hazme acuerdo de",
        "hazme acuerdo",
        "recuérdame que quiero",
        "recuerdame que quiero",
        "recuérdame que",
        "recuerdame que",
        "recuérdame de",
        "recuerdame de",
        "recuérdame",
        "recuerdame",
        "acuérdame que",
        "acuerdame que",
        "acuérdame de",
        "acuerdame de",
        "acuérdame",
        "acuerdame",
        "recordarme que",
        "recordarme de",
        "recordarme",
        "recordar que",
        "recordar",
        "no te olvides de que",
        "no te olvides de",
        "no te olvides",
        "no olvidar que",
        "no olvidar",
        "tengo que",
        "tenemos que",
        "hay que",
        "debo de",
        "debo",
        "quiero que",
        "quisiera que",
        "necesito que",
        "crear una nueva tarea para",
        "crear una nueva tarea de",
        "crear una nueva tarea",
        "crear una tarea para",
        "crear una tarea de",
        "crear una tarea",
        "crear tarea para",
        "crear tarea de",
        "crear tarea",
        "crea una nueva tarea",
        "crea una tarea",
        "crea tarea",
        "agregar una nueva tarea",
        "agregar una tarea",
        "agregar tarea",
        "agrega una tarea",
        "agrega tarea",
        "anota que",
        "anotar que",
        "anotar",
        "anota",
        "agendar que",
        "agenda que",
        "agendar",
        "agenda",
        "nuevo recordatorio para",
        "nuevo recordatorio",
        "crear recordatorio",
        "recordatorio de",
        "recordatorio para",
        "recordatorio",
        "por favor",
        "porfa",
        "quiero",
        "quisiera",
        "necesito"
    )

    private val TEMPORAL_REGEXES = listOf(
        // "para hoy en la tarde aprox 8 pm", "para el lunes en la noche a las 9 pm"
        Regex("""(?i)\b(?:para\s+)?(?:el\s+)?(?:hoy|mañana|manana|pasado\s+mañana|lunes|martes|miércoles|miercoles|jueves|viernes|sábado|sabado|domingo)\s+(?:en|de|por)\s+la\s+(?:mañana|tarde|noche)(?:\s+(?:a\s+las|a\s+la|a|aprox|aproximadamente)\s+\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.)?)?"""),
        // "para hoy a las 8 pm", "para el lunes a las 9 am", "el viernes a las 3 pm"
        Regex("""(?i)\b(?:para\s+)?(?:el\s+)?(?:hoy|mañana|manana|pasado\s+mañana|lunes|martes|miércoles|miercoles|jueves|viernes|sábado|sabado|domingo)(?:\s+(?:a\s+las|a\s+la|a|aprox|aproximadamente)\s+\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.)?)?"""),
        // "en la tarde aprox 8 pm", "en la noche"
        Regex("""(?i)\b(?:en|de|por)\s+la\s+(?:mañana|tarde|noche)(?:\s+(?:a\s+las|a\s+la|a|aprox|aproximadamente)\s+\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.)?)?"""),
        // "que las 4:21", "que a las 4:21", "a las 8 pm", "a las 8:00 am", "aprox 8 pm", "las 4:21"
        Regex("""(?i)\b(?:que\s+)?(?:a\s+las|a\s+la|alas|las|a|aprox|aproximadamente)\s+\d{1,2}(?::\d{2})?\s*(?:am|pm|a\.m\.|p\.m\.)?"""),
        Regex("""(?i)\bque\s+las\s+\d{1,2}(?::\d{2})?\b"""),
        // "al mediodía", "al mediodia"
        Regex("""(?i)\b(?:al|el)\s+mediod[ií]a\b"""),
        // "en 1 hora", "dentro de 30 minutos"
        Regex("""(?i)\b(?:en|dentro\s+de)\s+\d+\s+(?:minuto|minutos|hora|horas|día|días|dia|dias)\b"""),
        // "para hoy", "para mañana"
        Regex("""(?i)\b(?:para\s+)?(?:hoy|mañana|manana|pasado\s+mañana)\b""")
    )

    fun clean(rawPrompt: String, category: String = ""): String {
        var text = rawPrompt.trim()

        // 1. Corregir typos de reconocimiento de voz y jerga
        for ((regex, replacement) in SLANG_REPLACEMENTS) {
            text = text.replace(regex, replacement)
        }

        // 2. Eliminar expresiones temporales del título (se agendan como hora de alarma)
        for (regex in TEMPORAL_REGEXES) {
            text = text.replace(regex, " ").trim()
        }

        // 3. Eliminar prefijos comunes
        var lower = text.lowercase(Locale("es", "ES"))
        for (prefix in PREFIXES) {
            if (lower.startsWith(prefix)) {
                text = text.substring(prefix.length).trim()
                lower = text.lowercase(Locale("es", "ES"))
                break
            }
        }

        // Eliminar conectores iniciales redundantes ("que ", "de ", "a ", "en ", "para ")
        val connectPrefixes = listOf("que ", "de ", "a ", "en ", "para ")
        for (conn in connectPrefixes) {
            if (text.lowercase(Locale("es", "ES")).startsWith(conn)) {
                text = text.substring(conn.length).trim()
                break
            }
        }

        // Eliminar "también" final que suele ser muletilla del dictado
        text = text.replace(Regex("""(?i)\s+tambi[eé]n\s*$"""), "").trim()

        // Eliminar conectores o preposiciones finales sueltas ("para", "de", "el", "la", "a", etc.)
        text = text.replace(Regex("""(?i)\s+(?:para|de|a|en|por|el|la)\s*$"""), "").trim()

        // 4. Limpieza de espacios múltiples y signos de puntuación sueltos
        text = text.replace(Regex("""\s+"""), " ")
            .trim(',', '.', ';', ':', ' ')

        if (text.isBlank()) {
            return rawPrompt.trim().replaceFirstChar { it.titlecase(Locale("es", "ES")) }
        }

        // 5. Agregar verbo de acción si hace falta
        val textLower = text.lowercase(Locale("es", "ES"))
        val isShopping = category == "SHOPPING" ||
            listOf("pan", "leche", "huevos", "fruta", "carne", "comida", "mercado", "super", "tienda").any { textLower.contains(it) }

        val commonVerbs = listOf("comprar", "hacer", "llamar", "ir", "pagar", "enviar", "preparar", "revisar", "pedir", "traer", "recoger", "terminar", "estudiar")
        val startsWithVerb = commonVerbs.any { textLower.startsWith(it) }

        if (!startsWithVerb) {
            if (isShopping) {
                text = "Comprar $text"
            } else if (textLower.startsWith("informe") || textLower.startsWith("reporte") || textLower.startsWith("presentación")) {
                text = "Preparar $text"
            }
        }

        // 6. Capitalizar primera letra
        return text.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString()
        }
    }
}

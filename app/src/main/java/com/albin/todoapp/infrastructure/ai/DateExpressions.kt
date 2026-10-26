package com.albin.todoapp.infrastructure.ai

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.regex.Pattern

/**
 * Interpreta expresiones temporales en lenguaje natural en español hacia un LocalDateTime exacto,
 * siguiendo la arquitectura de DateExpressions de Philipp Lackner (TodoAI).
 */
object DateExpressions {

    fun parseDateTime(input: String, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val lower = input.lowercase(Locale("es", "ES")).trim()

        // 1. Extraer hora (HH:mm, H am/pm, etc.)
        val parsedTime = extractTime(lower, now)

        // 2. Extraer día (hoy, mañana, día de la semana, etc.)
        val parsedDate = extractDate(lower, now.toLocalDate())

        val targetDate = parsedDate ?: now.toLocalDate()
        val targetTime = parsedTime ?: run {
            if (parsedDate != null && parsedDate.isAfter(now.toLocalDate())) {
                LocalTime.of(9, 0) // Si es mañana u otro día futuro sin hora, 9:00 AM
            } else {
                now.toLocalTime().plusHours(1).withMinute(0).withSecond(0) // En 1 hora
            }
        }

        var candidate = LocalDateTime.of(targetDate, targetTime)

        // Si la hora resultante ya pasó por más de 15 minutos hoy y no se especificó un día explícito, mover a mañana o +12h
        if (candidate.isBefore(now.minusMinutes(15)) && parsedDate == null) {
            candidate = if (candidate.plusHours(12).isAfter(now)) {
                candidate.plusHours(12)
            } else {
                candidate.plusDays(1)
            }
        }

        return candidate
    }

    private fun extractTime(text: String, now: LocalDateTime = LocalDateTime.now()): LocalTime? {
        // Expresiones contextuales comunes
        if (text.contains("en la noche") || text.contains("de la noche") || text.contains("por la noche")) {
            val hour = extractSingleHour(text) ?: 20
            return LocalTime.of(if (hour < 12) hour + 12 else hour, extractMinutes(text) ?: 0)
        }
        if (text.contains("en la tarde") || text.contains("de la tarde") || text.contains("por la tarde")) {
            val hour = extractSingleHour(text) ?: 16
            return LocalTime.of(if (hour < 12) hour + 12 else hour, extractMinutes(text) ?: 0)
        }
        if (text.contains("al mediodía") || text.contains("al mediodia")) {
            return LocalTime.of(12, 0)
        }
        if (text.contains("en la mañana") || text.contains("por la mañana") || text.contains("de la mañana")) {
            val hour = extractSingleHour(text) ?: 9
            return LocalTime.of(if (hour >= 12) hour - 12 else hour, extractMinutes(text) ?: 0)
        }

        // Patrón para "que las 4:21", "a las 7:30 pm", "7:30pm", "a las 7 pm", "7pm", "19:30"
        val timeRegex = Pattern.compile("""(?:que\s+las\s+|que\s+a\s+las\s+|a\s+las\s+|a\s+la\s+|alas\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm|a\.m\.|p\.m\.)?""")
        val matcher = timeRegex.matcher(text)

        while (matcher.find()) {
            val hourStr = matcher.group(1) ?: continue
            var hour = hourStr.toIntOrNull() ?: continue
            val minute = matcher.group(2)?.toIntOrNull() ?: 0
            val amPm = matcher.group(3)?.replace(".", "")?.lowercase()

            if (hour in 0..23 && minute in 0..59) {
                if (amPm == "pm" && hour < 12) {
                    hour += 12
                } else if (amPm == "am" && hour == 12) {
                    hour = 0
                } else if (amPm == null && hour in 1..11 && now.hour >= 12) {
                    // Si estamos en la tarde y el usuario dice una hora de tarde sin am/pm (ej: 4:21 cuando son las 4pm)
                    if (hour + 12 >= now.hour - 1) {
                        hour += 12
                    }
                }
                return LocalTime.of(hour, minute)
            }
        }

        // "y media", ej: "a las 3 y media"
        val halfPastRegex = Pattern.compile("""(?:a\s+las\s+|alas\s+)(\d{1,2})\s+y\s+media""")
        val halfMatcher = halfPastRegex.matcher(text)
        if (halfMatcher.find()) {
            val hour = halfMatcher.group(1)?.toIntOrNull() ?: return null
            val finalHour = if (hour in 1..7 && (text.contains("tarde") || hour <= 6)) hour + 12 else hour
            return LocalTime.of(finalHour, 30)
        }

        return null
    }

    private fun extractSingleHour(text: String): Int? {
        val regex = Pattern.compile("""(?:que\s+las\s+|que\s+a\s+las\s+|a\s+las\s+|a\s+la\s+|alas\s+)?(\d{1,2})""")
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull()
            if (h != null && h in 1..24) return h
        }
        return null
    }

    private fun extractMinutes(text: String): Int? {
        val regex = Pattern.compile(""":(\d{2})""")
        val matcher = regex.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        if (text.contains("y media")) return 30
        if (text.contains("y cuarto")) return 15
        return null
    }

    private fun extractDate(text: String, today: LocalDate): LocalDate? {
        if (text.contains("pasado mañana")) return today.plusDays(2)
        if (text.contains("mañana") || text.contains("manana")) return today.plusDays(1)
        if (text.contains("hoy")) return today

        // Días de la semana
        val days = mapOf(
            "lunes" to DayOfWeek.MONDAY,
            "martes" to DayOfWeek.TUESDAY,
            "miércoles" to DayOfWeek.WEDNESDAY,
            "miercoles" to DayOfWeek.WEDNESDAY,
            "jueves" to DayOfWeek.THURSDAY,
            "viernes" to DayOfWeek.FRIDAY,
            "sábado" to DayOfWeek.SATURDAY,
            "sabado" to DayOfWeek.SATURDAY,
            "domingo" to DayOfWeek.SUNDAY
        )

        for ((dayName, dayOfWeek) in days) {
            if (text.contains(dayName)) {
                return today.with(TemporalAdjusters.next(dayOfWeek))
            }
        }

        return null
    }

    /**
     * Formatea un LocalDateTime en lenguaje natural claro en español para que el asistente responda al usuario.
     */
    fun formatSpokenDateTime(dateTime: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val date = dateTime.toLocalDate()
        val today = now.toLocalDate()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "ES"))
        val formattedTime = dateTime.format(timeFormatter)

        val dateLabel = when (date) {
            today -> "hoy"
            today.plusDays(1) -> "mañana"
            today.plusDays(2) -> "pasado mañana"
            else -> {
                val dateFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
                date.format(dateFormatter)
            }
        }

        return "$dateLabel a las $formattedTime"
    }
}

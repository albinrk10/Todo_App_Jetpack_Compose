package com.albin.todoapp.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilidades centrales para formateo y parseo de fechas y horas en español.
 */
object DateTimeUtils {

    val SPANISH_LOCALE: Locale = Locale("es", "ES")

    private val fullDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", SPANISH_LOCALE)

    private val dateWithTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM, hh:mm a", SPANISH_LOCALE)

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", SPANISH_LOCALE)

    private val dayMonthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM", SPANISH_LOCALE)

    fun formatFullDate(date: LocalDate): String =
        date.format(fullDateFormatter)

    fun formatDateWithTime(dateTime: LocalDateTime): String =
        dateTime.format(dateWithTimeFormatter)

    fun formatTime(time: LocalTime): String =
        time.format(timeFormatter)

    fun formatDayMonth(date: LocalDate): String =
        date.format(dayMonthFormatter)

    fun formatCapitalizedFullDate(date: LocalDate = LocalDate.now()): String {
        val formatted = formatFullDate(date)
        return formatted.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(SPANISH_LOCALE) else it.toString()
        }
    }
}

package com.albin.todoapp.infrastructure.ai

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateExpressionsTest {

    private val fixedNow = LocalDateTime.of(2026, 10, 25, 10, 0) // Domingo 25 oct 2026, 10:00 AM

    @Test
    fun `parseDateTime with hoy a las 7pm returns today 19h00`() {
        val result = DateExpressions.parseDateTime("hoy a las 7:00 pm", fixedNow)
        assertEquals(LocalDate.of(2026, 10, 25), result.toLocalDate())
        assertEquals(LocalTime.of(19, 0), result.toLocalTime())
    }

    @Test
    fun `parseDateTime with manana a las 10 am returns tomorrow 10h00`() {
        val result = DateExpressions.parseDateTime("mañana a las 10 am", fixedNow)
        assertEquals(LocalDate.of(2026, 10, 26), result.toLocalDate())
        assertEquals(LocalTime.of(10, 0), result.toLocalTime())
    }

    @Test
    fun `parseDateTime with en la tarde assigns 16h00`() {
        val result = DateExpressions.parseDateTime("hoy en la tarde", fixedNow)
        assertEquals(LocalDate.of(2026, 10, 25), result.toLocalDate())
        assertEquals(LocalTime.of(16, 0), result.toLocalTime())
    }

    @Test
    fun `formatSpokenDateTime formats properly in Spanish`() {
        val dt = LocalDateTime.of(2026, 10, 25, 19, 30)
        val spoken = DateExpressions.formatSpokenDateTime(dt, fixedNow)
        assertEquals("hoy a las 07:30 p. m.", spoken.replace("pm", "p. m.").lowercase())
    }
}

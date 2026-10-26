package com.albin.todoapp.infrastructure.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTitleCleanerTest {

    @Test
    fun `cleans user screenshot colloquial prompt and extracts clean shopping title`() {
        val input = "hasme acordar que quiero pan para hoy en la tarde aprox 8 pm para mi mana tbm"
        val cleaned = TaskTitleCleaner.clean(input, category = "SHOPPING")

        assertEquals("Comprar pan para mi hermana", cleaned)
    }

    @Test
    fun `cleans hazme acordar with temporal expression`() {
        val input = "hazme acordar comprar leche mañana a las 10:00 am"
        val cleaned = TaskTitleCleaner.clean(input, category = "SHOPPING")

        assertEquals("Comprar leche", cleaned)
    }

    @Test
    fun `cleans recuerdame prompt without verb for shopping item`() {
        val input = "recuerdame pan para hoy a las 8 pm"
        val cleaned = TaskTitleCleaner.clean(input, category = "SHOPPING")

        assertEquals("Comprar pan", cleaned)
    }

    @Test
    fun `cleans personal prompt calling doctor`() {
        val input = "hazme acuerdo de llamar al dentista el viernes en la mañana"
        val cleaned = TaskTitleCleaner.clean(input, category = "PERSONAL")

        assertEquals("Llamar al dentista", cleaned)
    }

    @Test
    fun `cleans work prompt for report preparation`() {
        val input = "anota informe financiero mensual para el lunes a las 9 am"
        val cleaned = TaskTitleCleaner.clean(input, category = "WORK")

        assertEquals("Preparar informe financiero mensual", cleaned)
    }

    @Test
    fun `cleans user speech recognition with typos and time prefix`() {
        val input = "que las 4:21 te go que comprar pan para mis hermnaos"
        val cleaned = TaskTitleCleaner.clean(input, category = "SHOPPING")

        assertEquals("Comprar pan para mis hermanos", cleaned)
    }

    @Test
    fun `normalizes slang mano and tbm`() {
        val input = "quiero enviar fotos a mi mano tbm"
        val cleaned = TaskTitleCleaner.clean(input)

        assertEquals("Enviar fotos a mi hermano", cleaned)
    }
}

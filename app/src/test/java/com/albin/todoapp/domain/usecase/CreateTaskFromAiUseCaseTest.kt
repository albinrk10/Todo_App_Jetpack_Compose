package com.albin.todoapp.domain.usecase

import com.albin.todoapp.domain.Category
import com.albin.todoapp.testutil.TestTaskLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CreateTaskFromAiUseCaseTest {

    private lateinit var dataSource: TestTaskLocalDataSource
    private lateinit var useCase: CreateTaskFromAiUseCase

    @Before
    fun setUp() {
        dataSource = TestTaskLocalDataSource()
        useCase = CreateTaskFromAiUseCase(dataSource)
    }

    @Test
    fun `invoke with shopping title resolves SHOPPING category and adds task`() = runTest {
        val task = useCase(
            title = "Comprar leche deslactosada",
            description = "2 cartones",
            dueDate = "mañana"
        )

        assertEquals("Comprar leche deslactosada", task.title)
        assertEquals("2 cartones", task.description)
        assertEquals(Category.SHOPPING, task.category)
        assertFalse(task.isCompleted)
        assertEquals(LocalDate.now().plusDays(1), task.date.toLocalDate())

        val savedTasks = dataSource.tasksFlow.first()
        assertEquals(1, savedTasks.size)
        assertEquals(task.id, savedTasks.first().id)
    }

    @Test
    fun `invoke with work title resolves WORK category`() = runTest {
        val task = useCase(
            title = "Reunión de planificación del proyecto",
            dueDate = "hoy"
        )

        assertEquals(Category.WORK, task.category)
        assertEquals(LocalDate.now(), task.date.toLocalDate())
    }

    @Test
    fun `invoke with explicit category parameter overrides inference`() = runTest {
        val task = useCase(
            title = "Comprar herramientas de oficina",
            categoryName = "WORK"
        )
        assertEquals(Category.WORK, task.category)
    }

    @Test
    fun `invoke with ISO dueDate preserves exact hour and minute`() = runTest {
        val isoDate = "2026-10-25T16:21:00"
        val task = useCase(
            title = "Comprar pan para mis hermanos",
            dueDate = isoDate
        )
        assertEquals(16, task.date.hour)
        assertEquals(21, task.date.minute)
    }
}

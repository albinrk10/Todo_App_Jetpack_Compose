package com.albin.todoapp.infrastructure.ai

import com.albin.todoapp.domain.Category
import com.albin.todoapp.domain.Task
import com.albin.todoapp.domain.usecase.CreateTaskFromAiUseCase
import com.albin.todoapp.domain.usecase.GetTasksForAiUseCase
import com.albin.todoapp.testutil.TestTaskLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TodoAiAgentTest {

    private lateinit var dataSource: TestTaskLocalDataSource
    private lateinit var createTaskUseCase: CreateTaskFromAiUseCase
    private lateinit var getTasksUseCase: GetTasksForAiUseCase
    private lateinit var createTaskTool: CreateTaskTool
    private lateinit var getTasksTool: GetTasksTool
    private lateinit var searchTasksTool: SearchTasksTool
    private lateinit var setTaskDoneTool: SetTaskDoneTool
    private lateinit var engine: LocalLlmEngine
    private lateinit var agent: TodoAiAgent

    @Before
    fun setUp() {
        dataSource = TestTaskLocalDataSource()
        createTaskUseCase = CreateTaskFromAiUseCase(dataSource)
        getTasksUseCase = GetTasksForAiUseCase(dataSource)
        createTaskTool = CreateTaskTool(createTaskUseCase)
        getTasksTool = GetTasksTool(getTasksUseCase)
        searchTasksTool = SearchTasksTool(getTasksUseCase)
        setTaskDoneTool = SetTaskDoneTool(dataSource)
        engine = LocalLlmEngine()
        agent = TodoAiAgent(engine, createTaskTool, getTasksTool, searchTasksTool, setTaskDoneTool)
    }

    @Test
    fun `process create task prompt triggers createTaskTool and saves task to dataSource`() = runTest {
        val events = agent.process("Recuérdame comprar pan mañana a las 7:00 pm").toList()

        assertTrue(events.any { it is AgentEvent.ToolExecuting && it.toolName == "create_task" })
        val completedEvent = events.last() as AgentEvent.Completed
        assertTrue(completedEvent.responseText.contains("pan"))

        val savedTasks = dataSource.tasksFlow.first()
        assertEquals(1, savedTasks.size)
        assertTrue(savedTasks.first().title.contains("pan"))
        assertEquals(Category.SHOPPING, savedTasks.first().category)
    }

    @Test
    fun `process colloquial prompt with slang creates clean task with reminder`() = runTest {
        val prompt = "hasme acordar que quiero pan para hoy en la tarde aprox 8 pm para mi mana tbm"
        val events = agent.process(prompt).toList()

        val toolEvent = events.first { it is AgentEvent.ToolExecuting } as AgentEvent.ToolExecuting
        assertEquals("create_task", toolEvent.toolName)
        assertEquals("Motor Local NLP", toolEvent.engineName)
        assertEquals("Comprar pan para mi hermana", toolEvent.arguments)

        val savedTasks = dataSource.tasksFlow.first()
        assertEquals(1, savedTasks.size)
        assertEquals("Comprar pan para mi hermana", savedTasks.first().title)
        assertEquals(Category.SHOPPING, savedTasks.first().category)
    }

    @Test
    fun `process conversational message like ok replies friendly without creating any task`() = runTest {
        val events = agent.process("ok").toList()

        // Debe emitir solo Completed y NO ToolExecuting
        assertTrue(events.none { it is AgentEvent.ToolExecuting })
        val completed = events.first { it is AgentEvent.Completed } as AgentEvent.Completed
        assertTrue(completed.responseText.contains("Con gusto"))

        // La base de datos no debe tener tareas creadas
        val savedTasks = dataSource.tasksFlow.first()
        assertEquals(0, savedTasks.size)
    }

    @Test
    fun `process speech recognition with typos creates clean task Comprar pan para mis hermanos`() = runTest {
        val prompt = "que las 4:21 te go que comprar pan para mis hermnaos"
        val events = agent.process(prompt).toList()

        val toolEvent = events.first { it is AgentEvent.ToolExecuting } as AgentEvent.ToolExecuting
        assertEquals("create_task", toolEvent.toolName)
        assertEquals("Comprar pan para mis hermanos", toolEvent.arguments)

        val savedTasks = dataSource.tasksFlow.first()
        assertEquals(1, savedTasks.size)
        assertEquals("Comprar pan para mis hermanos", savedTasks.first().title)
    }

    @Test
    fun `process query tasks prompt triggers getTasksTool and returns list summary`() = runTest {
        dataSource.addTask(
            Task(
                id = "1",
                title = "Entregar reporte financiero",
                description = null,
                isCompleted = false,
                category = Category.WORK,
                date = LocalDateTime.now()
            )
        )

        val events = agent.process("¿Qué tareas tengo pendientes?").toList()

        assertTrue(events.any { it is AgentEvent.ToolExecuting && it.toolName == "get_tasks" })
        val completed = events.last() as AgentEvent.Completed
        assertTrue(completed.responseText.contains("Entregar reporte financiero"))
    }

    @Test
    fun `process set task done marks existing task as completed`() = runTest {
        dataSource.addTask(
            Task(
                id = "2",
                title = "Comprar leche",
                description = null,
                isCompleted = false,
                category = Category.SHOPPING,
                date = LocalDateTime.now()
            )
        )

        val events = agent.process("Completar comprar leche").toList()
        assertTrue(events.any { it is AgentEvent.ToolExecuting && it.toolName == "set_task_done" })

        val savedTasks = dataSource.tasksFlow.first()
        assertTrue(savedTasks.first { it.id == "2" }.isCompleted)
    }
}

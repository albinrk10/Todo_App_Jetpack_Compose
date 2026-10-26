package com.albin.todoapp.presentation.screens.assistant

import com.albin.todoapp.domain.ai.AiAgentRepository
import com.albin.todoapp.domain.ai.AiMessage
import com.albin.todoapp.domain.ai.ModelState
import com.albin.todoapp.presentation.screens.assistant.interactor.AssistantUiIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeAiAgentRepository : AiAgentRepository {
    val _modelState = MutableStateFlow<ModelState>(ModelState.NotDownloaded)
    override val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    override val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    val processedPrompts = mutableListOf<String>()
    var downloadCalled = false

    override suspend fun downloadModel() {
        downloadCalled = true
        _modelState.value = ModelState.Downloading(0.5f)
    }

    override suspend fun processUserPrompt(prompt: String) {
        processedPrompts.add(prompt)
        _messages.value = _messages.value + AiMessage(text = prompt, isUser = true)
    }

    override suspend fun clearHistory() {
        _messages.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAiAgentRepository
    private lateinit var viewModel: AssistantViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAiAgentRepository()
        viewModel = AssistantViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ChangeInputText intent updates uiState inputText`() {
        viewModel.onIntent(AssistantUiIntent.ChangeInputText("Comprar pan"))
        assertEquals("Comprar pan", viewModel.uiState.value.inputText)
    }

    @Test
    fun `SendMessage intent clears inputText and sends prompt to repository`() = runTest {
        viewModel.onIntent(AssistantUiIntent.ChangeInputText("Comprar leche"))
        viewModel.onIntent(AssistantUiIntent.SendMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.inputText)
        assertEquals(listOf("Comprar leche"), repository.processedPrompts)
    }

    @Test
    fun `VoiceResult intent sends transcribed prompt immediately to repository`() = runTest {
        viewModel.onIntent(AssistantUiIntent.VoiceResult("Llamar al doctor mañana"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("Llamar al doctor mañana"), repository.processedPrompts)
    }

    @Test
    fun `DownloadModel intent delegates call to repository`() = runTest {
        viewModel.onIntent(AssistantUiIntent.DownloadModel)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, repository.downloadCalled)
    }
}

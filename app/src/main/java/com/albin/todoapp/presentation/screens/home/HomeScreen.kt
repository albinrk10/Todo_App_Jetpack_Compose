@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.albin.todoapp.presentation.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.albin.todoapp.R
import com.albin.todoapp.domain.Category
import com.albin.todoapp.domain.Task
import com.albin.todoapp.presentation.screens.assistant.AssistantBottomSheet
import com.albin.todoapp.presentation.screens.assistant.AssistantViewModel
import com.albin.todoapp.presentation.screens.home.components.SectionTitle
import com.albin.todoapp.presentation.screens.home.components.SummaryInfo
import com.albin.todoapp.presentation.screens.home.components.TaskItem
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiEffect
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiIntent
import com.albin.todoapp.presentation.screens.home.interactor.HomeUiState
import com.albin.todoapp.core.theme.TodoAppTheme

@Composable
fun HomeScreenRoot(
    navigateToTaskScreen: (String?) -> Unit,
    viewModel: HomeScreenViewModel,
    assistantViewModel: AssistantViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val event = viewModel.events
    val context = LocalContext.current
    var showAssistantSheet by remember { mutableStateOf(false) }

    val assistantUiState by assistantViewModel.uiState.collectAsState()

    // Request Notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) {}
        LaunchedEffect(Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(true) {
        event.collect { event ->
            when (event) {
                HomeUiEffect.DeletedTask -> {
                    Toast.makeText(context, context.getString(R.string.task_deleted), Toast.LENGTH_SHORT).show()
                }
                HomeUiEffect.AllTaskDeleted -> {
                    Toast.makeText(context, context.getString(R.string.all_task_deleted), Toast.LENGTH_SHORT).show()
                }
                HomeUiEffect.UpdatedTask -> {
                    Toast.makeText(context, context.getString(R.string.task_updated), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HomeUiIntent.OnAddTask -> {
                    navigateToTaskScreen(null)
                }
                is HomeUiIntent.OnClickTask -> {
                    navigateToTaskScreen(action.taskId)
                }
                is HomeUiIntent.OnOpenAssistant -> {
                    showAssistantSheet = true
                }
                else -> viewModel.onIntent(action)
            }
        }
    )

    if (showAssistantSheet) {
        AssistantBottomSheet(
            onDismissRequest = { showAssistantSheet = false },
            uiState = assistantUiState,
            onIntent = assistantViewModel::onIntent
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState,
    onAction: (HomeUiIntent) -> Unit
) {
    var isMenuExtended by remember { mutableStateOf(false) }
    val hasTasks = state.completedTask.isNotEmpty() || state.pendingTask.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón único para abrir el Asistente
                    IconButton(
                        onClick = { onAction(HomeUiIntent.OnOpenAssistant) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chat),
                            contentDescription = stringResource(R.string.assistant),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Menú de 3 puntos: SOLO visible si hay notas
                    if (hasTasks) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    isMenuExtended = true
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            DropdownMenu(
                                expanded = isMenuExtended,
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                onDismissRequest = { isMenuExtended = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.delete_all),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        isMenuExtended = false
                                        onAction(HomeUiIntent.OnDeleteAllTasks)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            if (!hasTasks) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_tasks),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues = paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SummaryInfo(
                            date = state.date,
                            tasksSummary = stringResource(R.string.summary, state.summary),
                            completedTasks = state.completedTask.size,
                            totalTask = state.completedTask.size + state.pendingTask.size
                        )
                    }

                    stickyHeader {
                        SectionTitle(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.surface)
                                .fillParentMaxWidth(),
                            title = stringResource(R.string.pending_tasks)
                        )
                    }

                    items(
                        items = state.pendingTask,
                        key = { task -> task.id }
                    ) { task ->
                        TaskItem(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .animateItem(),
                            task = task,
                            onClickItem = {
                                onAction(HomeUiIntent.OnClickTask(task.id))
                            },
                            onDeleteItem = {
                                onAction(HomeUiIntent.OnDeleteTask(task))
                            },
                            onToggleCompletion = {
                                onAction(HomeUiIntent.OnToggleTask(it))
                            }
                        )
                    }

                    stickyHeader {
                        SectionTitle(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface),
                            title = stringResource(R.string.completed_tasks)
                        )
                    }

                    items(
                        items = state.completedTask,
                        key = { task -> task.id }
                    ) { task ->
                        TaskItem(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .animateItem(),
                            task = task,
                            onClickItem = {
                                onAction(HomeUiIntent.OnClickTask(task.id))
                            },
                            onDeleteItem = {
                                onAction(HomeUiIntent.OnDeleteTask(task))
                            },
                            onToggleCompletion = {
                                onAction(HomeUiIntent.OnToggleTask(it))
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Un solo botón flotante para crear tareas
            FloatingActionButton(
                onClick = {
                    onAction(HomeUiIntent.OnAddTask)
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    )
}

@Preview
@Composable
fun HomeScreenPreviewLight() {
    TodoAppTheme {
        HomeScreen(
            state = HomeUiState(
                date = "Domingo, 25 de octubre 2026",
                summary = "1",
                pendingTask = listOf(
                    Task(
                        id = "1",
                        title = "Comprar micrófono",
                        description = "Recordatorio para compras pendientes",
                        category = Category.SHOPPING,
                        isCompleted = false
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HomeScreenPreviewDark() {
    TodoAppTheme {
        HomeScreen(
            state = HomeUiState(
                date = "Domingo, 25 de octubre 2026",
                summary = "1",
                pendingTask = listOf(
                    Task(
                        id = "1",
                        title = "Comprar micrófono",
                        description = "Recordatorio para compras pendientes",
                        category = Category.SHOPPING,
                        isCompleted = false
                    )
                )
            ),
            onAction = {}
        )
    }
}

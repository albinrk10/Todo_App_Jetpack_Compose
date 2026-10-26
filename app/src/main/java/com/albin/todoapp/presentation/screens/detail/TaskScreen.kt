package com.albin.todoapp.presentation.screens.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.albin.todoapp.R
import com.albin.todoapp.domain.Category
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiEffect
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiIntent
import com.albin.todoapp.presentation.screens.detail.interactor.TaskDetailUiState

import com.albin.todoapp.core.theme.TodoAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskScreenRoot(
    navigateBack: () -> Boolean,
    viewModel: TaskViewModel
) {
    val state = viewModel.state
    val event = viewModel.event

    val context = LocalContext.current

    LaunchedEffect(true) {
        event.collect { event ->
            when (event) {
                is TaskDetailUiEffect.TaskCreated -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.task_created),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateBack()
                }
            }
        }
    }

    TaskScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is TaskDetailUiIntent.Back -> {
                    navigateBack()
                }
                else -> {
                    viewModel.onIntent(action)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    state: TaskDetailUiState,
    onAction: (TaskDetailUiIntent) -> Unit
) {
    val context = LocalContext.current
    var isDescriptionFocus by remember {
        mutableStateOf(false)
    }
    var isExpanded by remember {
        mutableStateOf(false)
    }

    val showDateTimePicker: () -> Unit = {
        val current = state.date ?: LocalDateTime.now().plusHours(1)
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                        onAction(TaskDetailUiIntent.ChangeTaskDateTime(selectedDateTime))
                    },
                    current.hour,
                    current.minute,
                    DateFormat.is24HourFormat(context)
                ).show()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        style = MaterialTheme.typography.headlineSmall,
                        text = stringResource(R.string.task)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable {
                            onAction(TaskDetailUiIntent.Back)
                        }
                    )
                },
            )
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(8.dp)
                )
                Checkbox(
                    checked = state.isTaskDone,
                    onCheckedChange = {
                        onAction(
                            TaskDetailUiIntent.ChangeTaskDone(
                                isTaskDone = it
                            )
                        )
                    },
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isExpanded = true
                    }
                ) {
                    Text(
                        text = state.category?.displayName ?: stringResource(R.string.category),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Categoría",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        DropdownMenu(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            Column {
                                Category.entries.forEach { category ->
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .padding(8.dp)
                                            .clickable {
                                                isExpanded = false
                                                onAction(
                                                    TaskDetailUiIntent.ChangeTaskCategory(
                                                        category = category
                                                    )
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            BasicTextField(
                state = state.taskName,
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                decorator = { innerTextField ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.taskName.text.toString().isEmpty()) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.task_name),
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            innerTextField()
                        }
                    }
                }
            )

            BasicTextField(
                state = state.taskDescription,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                lineLimits = if (isDescriptionFocus) {
                    TextFieldLineLimits.MultiLine(
                        minHeightInLines = 1,
                        maxHeightInLines = 5
                    )
                } else {
                    TextFieldLineLimits.Default
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onFocusChanged {
                        isDescriptionFocus = it.isFocused
                    },
                decorator = { innerTextField ->
                    Column {
                        if (state.taskDescription.text.toString().isEmpty() && !isDescriptionFocus) {
                            Text(
                                text = stringResource(R.string.task_description),
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                )
                            )
                        } else {
                            innerTextField()
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DueDateRow following Philipp Lackner's TodoAI architecture
            DueDateRow(
                dateTime = state.date,
                onClick = showDateTimePicker,
                onClearClick = { onAction(TaskDetailUiIntent.ChangeTaskDateTime(null)) }
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                enabled = state.canSaveTask,
                onClick = {
                    onAction(
                        TaskDetailUiIntent.SaveTask
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.canSaveTask)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DueDateRow(
    dateTime: LocalDateTime?,
    onClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedText = remember(dateTime) {
        dateTime?.let {
            val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM, hh:mm a", Locale("es", "ES"))
            it.format(formatter)
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fecha y hora del recordatorio",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedText ?: "Sin fecha programada (Toca para agregar)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (formattedText != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (formattedText != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }
            if (dateTime != null) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar recordatorio",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun TaskScreenLightPreview(

) {
    TodoAppTheme {
        TaskScreen(
            state = TaskDetailUiState(
                taskName = androidx.compose.foundation.text.input.TextFieldState("Comprar micrófono"),
                canSaveTask = true
            ),
            onAction = {}
        )
    }
}

@Composable
@Preview(
    uiMode = UI_MODE_NIGHT_YES
)
fun TaskScreenDarkPreview(

) {
    TodoAppTheme {
        TaskScreen(
            state = TaskDetailUiState(
                taskName = androidx.compose.foundation.text.input.TextFieldState("Comprar micrófono"),
                canSaveTask = true
            ),
            onAction = {}
        )
    }
}

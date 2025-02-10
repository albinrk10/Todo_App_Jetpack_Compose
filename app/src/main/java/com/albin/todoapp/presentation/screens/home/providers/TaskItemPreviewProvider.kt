package com.albin.todoapp.presentation.screens.home.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.albin.todoapp.domain.Category
import com.albin.todoapp.domain.Task

class TaskItemPreviewProvider: PreviewParameterProvider<Task> {
    override val values: Sequence<Task>
        get() = sequenceOf(
            Task(
                id = "1",
                title = "Task 1",
                isCompleted = false,
                description = "Description 1",
                category = Category.WORK,
            ),

            Task(
                id = "2",
                title = "Task 2",
                isCompleted = true,
                description = "Description 2",
                category = Category.OTHER,
            ),
        )
}
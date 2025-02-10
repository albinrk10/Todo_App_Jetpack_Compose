package com.albin.todoapp.presentation.screens.detail

sealed interface TaskEvent{
    data object TaskCreated: TaskEvent
}
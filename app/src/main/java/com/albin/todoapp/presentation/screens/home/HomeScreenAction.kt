package com.albin.todoapp.presentation.screens.home
import com.albin.todoapp.domain.Task

sealed interface HomeScreenAction{
    data class OnClickTask(val taskId:String):HomeScreenAction
    data class OnToggleTask(val task:Task):HomeScreenAction
    data class OnDeleteTask(val task:Task):HomeScreenAction
    data object OnDeleteAllTasks:HomeScreenAction
    data object OnAddTask:HomeScreenAction
}
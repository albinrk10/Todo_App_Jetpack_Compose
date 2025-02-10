package com.albin.todoapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.albin.todoapp.presentation.screens.detail.TaskScreenRoot
import com.albin.todoapp.presentation.screens.detail.TaskViewModel
import com.albin.todoapp.presentation.screens.home.HomeScreenRoot
import com.albin.todoapp.presentation.screens.home.HomeScreenViewModel
import kotlinx.serialization.Serializable

@Composable
fun NavigationRoot (
    navController: NavHostController
){
    Box (
        modifier = Modifier.fillMaxSize()
    ){
        NavHost(
            navController = navController,
            startDestination = HomeScreenDes
        ){
            composable<HomeScreenDes>{
                val viewModel: HomeScreenViewModel = hiltViewModel()
                HomeScreenRoot(
                    navigateToTaskScreen = { taskId ->
                        navController.navigate(TaskScreenDes(
                            taskId = taskId
                        )
                        )
                    },
                    viewModel = viewModel
                )
            }

            composable<TaskScreenDes> {
                val taskViewModel : TaskViewModel = hiltViewModel()
                TaskScreenRoot(
                    viewModel = taskViewModel,
                    navigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Serializable
object HomeScreenDes

@Serializable
data class TaskScreenDes(val taskId: String? = null)
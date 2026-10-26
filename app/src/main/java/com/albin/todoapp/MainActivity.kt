package com.albin.todoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.rememberNavController
import com.albin.todoapp.presentation.navigation.HomeScreenDes
import com.albin.todoapp.presentation.navigation.NavigationRoot
import com.albin.todoapp.presentation.navigation.TaskScreenDes
import com.albin.todoapp.presentation.notifications.TaskAlarmReceiver
import com.albin.todoapp.core.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val pendingTaskId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val taskIdFromIntent = intent?.getStringExtra(TaskAlarmReceiver.EXTRA_TASK_ID)
            ?: intent?.getStringExtra("taskId")
        pendingTaskId.value = taskIdFromIntent

        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                val targetTaskId by pendingTaskId

                LaunchedEffect(targetTaskId) {
                    targetTaskId?.let { id ->
                        navController.navigate(TaskScreenDes(taskId = id)) {
                            popUpTo(HomeScreenDes) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                        pendingTaskId.value = null
                    }
                }

                NavigationRoot(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val taskIdFromIntent = intent.getStringExtra(TaskAlarmReceiver.EXTRA_TASK_ID)
            ?: intent.getStringExtra("taskId")
        pendingTaskId.value = taskIdFromIntent
    }
}

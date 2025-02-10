package com.albin.todoapp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.albin.todoapp.presentation.navigation.NavigationRoot
import com.albin.todoapp.ui.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme() {
               // HomeScreenRoot()
                val navController = rememberNavController()
               NavigationRoot(
                     navController = navController
               )
            }
//            TodoAppTheme() {
//                var text by remember { mutableStateOf("") }
//                val fakeTaskLocalDataSource = com.juandgaines.todoapp.data.FakeTaskLocalDataSource
//                LaunchedEffect(true) {
//                    fakeTaskLocalDataSource.taskFlow.collect{
//                      text = it.toString()
//                    }
//                }
//                LaunchedEffect(true) {
//                    fakeTaskLocalDataSource.addTask(
//                        Task(
//                            id = UUID.randomUUID().toString(),
//                            title = "Task 1",
//                            description = "Description 1"
//                        )
//                    )
//                }
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                   Text(
//                          text = text,
//                          modifier = Modifier
//                              .padding(top=innerPadding.calculateTopPadding())
//                              .fillMaxSize()
//                   )
//                }
//            }
        }
    }
}


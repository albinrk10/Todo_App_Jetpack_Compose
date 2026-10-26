package com.albin.todoapp.presentation.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.albin.todoapp.domain.TaskLocalDataSource
import com.albin.todoapp.domain.notifications.TaskNotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataSource: TaskLocalDataSource

    @Inject
    lateinit var scheduler: TaskNotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val tasks = dataSource.tasksFlow.first()
                for (task in tasks) {
                    if (!task.isCompleted) {
                        scheduler.schedule(task)
                    }
                }
            }
        }
    }
}

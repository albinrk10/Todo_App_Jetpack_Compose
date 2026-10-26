package com.albin.todoapp

import android.app.Application
import com.albin.todoapp.presentation.notifications.TaskAlarmReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskAlarmReceiver.createNotificationChannel(this)
    }
}

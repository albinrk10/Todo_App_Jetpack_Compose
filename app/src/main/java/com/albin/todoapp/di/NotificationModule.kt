package com.albin.todoapp.di

import com.albin.todoapp.domain.notifications.TaskNotificationScheduler
import com.albin.todoapp.presentation.notifications.AlarmManagerNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(
        impl: AlarmManagerNotificationScheduler
    ): TaskNotificationScheduler
}

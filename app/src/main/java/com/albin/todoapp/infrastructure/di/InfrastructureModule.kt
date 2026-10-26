package com.albin.todoapp.infrastructure.di

import android.content.Context
import androidx.room.Room
import com.albin.todoapp.domain.TaskLocalDataSource
import com.albin.todoapp.domain.notifications.TaskNotificationScheduler
import com.albin.todoapp.infrastructure.RoomTaskLocalDataSource
import com.albin.todoapp.infrastructure.TaskDao
import com.albin.todoapp.infrastructure.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InfrastructureModule {

    @Provides
    @Singleton
    fun provideDataBase(
        @ApplicationContext
        context: Context
    ): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "task_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(
        database: TodoDatabase
    ): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideTaskLocalDataSource(
        taskDao: TaskDao,
        scheduler: TaskNotificationScheduler,
        @Named("dispatcherIO")
        dispatcherIO: CoroutineDispatcher
    ): TaskLocalDataSource {
        return RoomTaskLocalDataSource(taskDao, scheduler, dispatcherIO)
    }
}

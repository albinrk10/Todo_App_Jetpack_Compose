package com.albin.todoapp.infrastructure
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class
    ],
    version = 1
)

abstract class TodoDatabase:RoomDatabase() {
    abstract fun taskDao(): TaskDao

}
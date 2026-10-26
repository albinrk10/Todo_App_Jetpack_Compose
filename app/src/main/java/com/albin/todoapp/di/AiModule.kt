package com.albin.todoapp.di

import com.albin.todoapp.infrastructure.repository.AiAgentRepositoryImpl
import com.albin.todoapp.domain.ai.AiAgentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiAgentRepository(
        impl: AiAgentRepositoryImpl
    ): AiAgentRepository
}

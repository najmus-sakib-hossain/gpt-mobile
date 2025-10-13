package dev.chungjungsoo.gptmobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.chungjungsoo.gptmobile.data.service.LLMService
import dev.chungjungsoo.gptmobile.data.service.LLMServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    @Binds
    @Singleton
    abstract fun bindLLMService(
        llmServiceImpl: LLMServiceImpl
    ): LLMService
}

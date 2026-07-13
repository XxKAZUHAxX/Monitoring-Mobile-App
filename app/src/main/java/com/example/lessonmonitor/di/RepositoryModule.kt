package com.example.lessonmonitor.di

import com.example.lessonmonitor.data.repository.AuthRepositoryImpl
import com.example.lessonmonitor.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain repository interfaces to their Phase-1 local-only
 * implementations. Additional `@Binds` are added here as each feature
 * milestone introduces its own repository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

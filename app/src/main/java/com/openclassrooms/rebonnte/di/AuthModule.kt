package com.openclassrooms.rebonnte.di

import com.openclassrooms.rebonnte.ui.auth.AuthRepository
import com.openclassrooms.rebonnte.ui.auth.AuthRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepository): AuthRepositoryInterface
}
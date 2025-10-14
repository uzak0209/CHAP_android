package com.example.chap.di

import android.content.Context
import com.example.chap.repository.AuthRepository
import com.example.chap.repository.AuthRepositoryImpl
import com.example.chap.location.LocationProvider
import com.example.chap.location.DefaultLocationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Singleton
    @Provides
    fun provideLocationProvider(@ApplicationContext context: Context): LocationProvider {
        return DefaultLocationProvider(context)
    }
}
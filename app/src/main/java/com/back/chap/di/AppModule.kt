package com.back.chap.di

import android.content.Context
import com.back.chap.repository.AuthRepository
import com.back.chap.repository.AuthRepositoryImpl
import com.back.chap.location.LocationProvider
import com.back.chap.location.DefaultLocationProvider
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
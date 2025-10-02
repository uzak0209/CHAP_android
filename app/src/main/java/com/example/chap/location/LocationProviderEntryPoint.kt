package com.example.chap.location

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocationProviderEntryPoint {
    fun locationProvider(): LocationProvider
}



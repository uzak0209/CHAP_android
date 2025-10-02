package com.example.chap.repository

import com.example.chap.models.Spot
import com.example.chap.models.PostCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    val spots: StateFlow<List<Spot>>
    suspend fun getSpotAll(): Result<List<Spot>>

    suspend fun createSpot(
        request: PostCreateRequest
    ): Result<String>
}


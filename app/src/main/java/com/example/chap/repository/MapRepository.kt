package com.example.chap.repository

import com.example.chap.models.Spot
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.SpotCreateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    val spots: StateFlow<List<Spot>>
    suspend fun getAllSpots(): Result<List<Spot>>
    suspend fun createSpot(
        request: SpotCreateRequest
    ): Result<Spot>
}


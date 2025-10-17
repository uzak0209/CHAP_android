package com.back.chap.repository

import com.back.chap.models.Spot
import com.back.chap.models.SpotCreateRequest
import kotlinx.coroutines.flow.StateFlow

interface MapRepository {
    val spots: StateFlow<List<Spot>>
    suspend fun getAllSpots(): Result<List<Spot>>
    suspend fun createSpot(
        request: SpotCreateRequest
    ): Result<Spot>
}


package com.example.chap.repository

import com.example.chap.models.Spot
import com.example.chap.models.PostCreateRequest

interface MapRepository {
    suspend fun getSpotAll(): Result<List<Spot>>

    suspend fun createSpot(
        request: PostCreateRequest
    ): Result<String>
}


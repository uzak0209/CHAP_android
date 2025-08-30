package com.example.chap.domain.repository

import com.example.chap.Models.Event
import com.example.chap.Models.PostCreateRequest
import kotlinx.coroutines.flow.StateFlow

interface EventRepository {
    val events: StateFlow<List<Event>>
    suspend fun getAll(): Result<List<Event>>
    suspend fun create(request: PostCreateRequest): Result<String>
}

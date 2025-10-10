package com.example.chap.repository

import com.example.chap.models.Event
import com.example.chap.models.PostCreateRequest
import kotlinx.coroutines.flow.StateFlow

interface EventRepository {
    val events: StateFlow<List<Event>>
    suspend fun getAllEvents(): Result<List<Event>>
    suspend fun createEvent(request: PostCreateRequest): Result<Event>
}

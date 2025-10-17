package com.back.chap.repository

import com.back.chap.models.Event
import com.back.chap.models.PostCreateRequest
import kotlinx.coroutines.flow.StateFlow

interface EventRepository {
    val events: StateFlow<List<Event>>
    suspend fun getAllEvents(): Result<List<Event>>
    suspend fun createEvent(request: PostCreateRequest): Result<Event>
}

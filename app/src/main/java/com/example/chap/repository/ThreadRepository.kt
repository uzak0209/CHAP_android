package com.example.chap.repository

import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Thread
import kotlinx.coroutines.flow.StateFlow

interface ThreadRepository {
    val threads: StateFlow<List<Thread>>
    suspend fun getAllThreads(): Result<List<Thread>>

    suspend fun createThread(thread: PostCreateRequest): Result<String>
    suspend fun getThreadById(id: String): Result<Thread>


}

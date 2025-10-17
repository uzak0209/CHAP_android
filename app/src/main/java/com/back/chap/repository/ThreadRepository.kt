package com.back.chap.repository

import com.back.chap.models.PostCreateRequest
import com.back.chap.models.Thread
import kotlinx.coroutines.flow.StateFlow

interface ThreadRepository {
    val threads: StateFlow<List<Thread>>
    suspend fun getAllThreads(): Result<List<Thread>>

    suspend fun createThread(thread: PostCreateRequest): Result<Thread>
    suspend fun getThreadById(id: String): Result<Thread>


}

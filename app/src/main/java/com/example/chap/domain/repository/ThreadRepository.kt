package com.example.chap.domain.repository

import com.example.chap.Models.PostCreateRequest
import com.example.chap.Models.Thread
import kotlinx.coroutines.flow.StateFlow

interface ThreadRepository {
    val threads: StateFlow<List<Thread>>
    suspend fun getAll(): Result<List<Thread>>

    suspend fun create(thread: PostCreateRequest): Result<String>
    suspend fun getThreadById(id: String): Result<Thread>


}

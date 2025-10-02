package com.example.chap.repository

import com.example.chap.models.Post
import com.example.chap.models.PostCreateRequest

interface PostRepository {
    suspend fun getAll(): Result<List<Post>>

    suspend fun create(
        request: PostCreateRequest
    ): Result<String>
}
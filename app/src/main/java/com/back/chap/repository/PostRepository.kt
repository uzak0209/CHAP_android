package com.back.chap.repository

import com.back.chap.models.Post
import com.back.chap.models.PostCreateRequest

interface PostRepository {
    suspend fun getAllPosts(): Result<List<Post>>

    suspend fun createPost(
        request: PostCreateRequest
    ): Result<Post>
}
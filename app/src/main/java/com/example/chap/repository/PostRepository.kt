package com.example.chap.repository

import com.example.chap.models.Coordinate
import com.example.chap.models.Post
import com.example.chap.models.PostCreateRequest

interface PostRepository {
    suspend fun getAllPosts(): Result<List<Post>>

    suspend fun createPost(
        request: PostCreateRequest
    ): Result<Post>
}
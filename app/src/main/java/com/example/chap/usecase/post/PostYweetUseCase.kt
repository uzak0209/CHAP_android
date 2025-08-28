package com.example.chap.usecase.post

import com.example.chap.Models.Coordinate
import com.example.chap.Models.User
import com.example.chap.domain.model.PostId

interface PostSubmitUseCase {
    suspend fun execute(
        id: PostId,
        type: String,
        created_at: String,
        updated_at: String,
        deleted_at: String?,
        user_id: String,
        username: String,
        user: User,
        coordinate: Coordinate,
        content: String,
        category: String,
        valid: Boolean,
        like: Int,
        tags: List<String>
    ): PostSubmitUseCaseResult
}
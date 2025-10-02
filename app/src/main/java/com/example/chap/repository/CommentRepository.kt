package com.example.chap.repository

import com.example.chap.models.Comment
import com.example.chap.models.RequestComment
import kotlinx.coroutines.flow.StateFlow

interface CommentRepository {
    val comments: StateFlow<List<Comment>>
    suspend fun getCommentsByThreadID(threadID:String): Result<List<Comment>>
    suspend fun createComment(comment:RequestComment): Result<String>
}
package com.example.chap.domain.repository

import com.example.chap.Models.Comment
import com.example.chap.Models.RequestComment
import kotlinx.coroutines.flow.StateFlow

interface CommentRepository {
    val comments: StateFlow<List<Comment>>
    suspend fun getCommentsByThreadID(threadID:String): Result<List<Comment>>
    suspend fun createComment(comment:RequestComment): Result<String>
}
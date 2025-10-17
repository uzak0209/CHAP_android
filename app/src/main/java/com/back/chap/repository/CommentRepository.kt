package com.back.chap.repository

import com.back.chap.models.Comment
import com.back.chap.models.RequestComment
import kotlinx.coroutines.flow.StateFlow

interface CommentRepository {
    val comments: StateFlow<List<Comment>>
    suspend fun getCommentsByThreadID(threadID:String): Result<List<Comment>>
    suspend fun createComment(comment:RequestComment): Result<String>
}
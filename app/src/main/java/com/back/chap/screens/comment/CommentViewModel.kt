package com.back.chap.screens.comment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.models.Comment
import com.back.chap.models.RequestComment
import com.back.chap.repository.CommentRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepositoryImpl
): ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    suspend fun getCommentsByThreadID(threadID:String): List<Comment> {
        val result: Result<List<Comment>> = commentRepository.getCommentsByThreadID(threadID)
        result.onSuccess {comments ->
            _comments.value = comments
        }.onFailure {e ->
            println("エラー: ${e.message}")
        }
        return _comments.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createComment(comment: RequestComment) {
        if (_isPosting.value) return
        viewModelScope.launch {
            _isPosting.value = true
            val result = commentRepository.createComment(comment)
            result.onSuccess { response ->
                println("コメント投稿成功: $response")
                // 投稿成功後にリロードしてUIを更新
                commentRepository.getCommentsByThreadID(comment.threadId)
                    .onSuccess { list ->
                        _comments.value = list
                        _errorMessage.value = null
                    }
                    .onFailure { e ->
                        _errorMessage.value = e.message ?: "Unknown error"
                    }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Unknown error"
                println("エラー: ${e.message}")
            }
            _isPosting.value = false
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(threadId: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            commentRepository.getCommentsByThreadID(threadId)
                .onSuccess { list ->
                    _comments.value = list
                    _errorMessage.value = null
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }
}


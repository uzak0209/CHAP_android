package com.example.chap.API

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Comment
import com.example.chap.Models.RequestComment
import com.example.chap.domain.repository.CommentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel(
    private val commentRepository: CommentRepositoryImpl
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
        viewModelScope.launch {
            val result = commentRepository.createComment(comment)
            result.onSuccess { response ->
                println("スレッド作成成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load(threadId: Long) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            commentRepository.getCommentsByThreadID(threadId.toString())
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

    fun refresh(threadId: Long) = load(threadId)
}

class CommentViewModelFactory(private val commentRepository: CommentRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentViewModel(commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

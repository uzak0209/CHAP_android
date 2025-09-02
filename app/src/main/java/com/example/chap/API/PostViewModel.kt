package com.example.chap.API


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.PostCreateRequest
import com.example.chap.Models.Post
import com.example.chap.domain.repository.PostRepositoryImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostViewModel(

    private val postRepository: PostRepositoryImpl
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    fun getPosts(): List<Post> {
        return postRepository.posts.value
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            postRepository.getAll().onSuccess { list ->
                _posts.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()

    fun getAllPosts() {
        viewModelScope.launch {
            val result = postRepository.getAll()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")

                // 必要に応じて_postの更新など
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: PostCreateRequest) {
        viewModelScope.launch {
            val result = postRepository.create(post)
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }
}


class PostViewModelFactory(private val postRepository: PostRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


package com.example.chap.API


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Post
import com.example.chap.domain.repository.PostRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PostViewModel(
    private val postRepository: PostRepositoryImpl = PostRepositoryImpl()
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: MutableStateFlow<List<Post>> = _posts

    fun getAllPosts() {
        viewModelScope.launch {
            val result = postRepository.getAllPostsApi()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")
                // 必要に応じて_postの更新など
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: Post) {
        viewModelScope.launch {
            val result = postRepository.createPostApi(post)
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }
}


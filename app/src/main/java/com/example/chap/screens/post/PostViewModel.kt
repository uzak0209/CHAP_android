package com.example.chap.screens.post


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Post
import org.json.JSONObject
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.screens.map.LocationViewModel
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
            // 位置情報未取得ならサーバーフィルタでヒットしない可能性があるため待機/スキップ
            val loc = LocationViewModel.locationState.location
            if (loc == null) {
                println("[PostViewModel] skip load: location not ready")
                _isLoading.value = false
                return@launch
            }
            postRepository.getAll().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _posts.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _posts.value = merged.sortedByDescending { it.created_at }
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


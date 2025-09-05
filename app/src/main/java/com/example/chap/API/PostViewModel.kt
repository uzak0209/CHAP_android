package com.example.chap.API


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.PostCreateRequest
import com.example.chap.Models.Post
import org.json.JSONObject
import com.example.chap.API.LocationViewModel
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: PostCreateRequest) {
        println("[PostViewModel] Starting post creation...")
        viewModelScope.launch {
            val result = postRepository.create(post)
            result.onSuccess { response ->
                println("[PostViewModel] 投稿成功: $response")
                // レスポンスJSONをPostにパースしてStateFlowへ即時反映
                try {
                    val json = JSONObject(response)
                    val created = Post(
                        id = json.optLong("id", 0L),
                        type = json.optString("type", ""),
                        created_at = json.optString("created_at", ""),
                        updated_at = json.optString("updated_at", ""),
                        deleted_at = if (json.isNull("deleted_at")) null else json.optString("deleted_at"),
                        user_id = json.optString("user_id", ""),
                        username = json.optString("username", ""),
                        coordinate = com.example.chap.Models.Coordinate(
                            lat = json.optJSONObject("coordinate")?.optDouble("lat", 0.0) ?: 0.0,
                            lng = json.optJSONObject("coordinate")?.optDouble("lng", 0.0) ?: 0.0,
                        ),
                        content = json.optString("content", ""),
                        category = json.optString("category", ""),
                        valid = json.optBoolean("valid", true),
                        like = json.optInt("like", 0),
                        tags = emptyList() // backend が tags 配列を返すなら parse へ拡張
                    )
                    _posts.value = (listOf(created) + _posts.value.filterNot { it.id == created.id })
                    println("[PostViewModel] Post added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("[PostViewModel] Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                println("[PostViewModel] 投稿エラー: ${e.message}")
                e.printStackTrace()
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


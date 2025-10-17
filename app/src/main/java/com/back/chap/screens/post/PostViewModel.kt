package com.back.chap.screens.post


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.location.LocationProvider
import com.back.chap.models.LocationState
import com.back.chap.models.Post
import com.back.chap.models.Status
import com.back.chap.repository.PostRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _locationState = MutableStateFlow(LocationState(null, Status.LOADING))
    val locationState: StateFlow<LocationState> = _locationState

    init {
        fetchAndUpdateLocation()
    }

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
            val loc = locationState.value.location
            if (loc == null) {
                println("[PostViewModel] skip load: location not ready")
                _isLoading.value = false
                return@launch
            }
            postRepository.getAllPosts().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _posts.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _posts.value = merged.sortedByDescending { it.createdAt }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun getAllPosts() {
        viewModelScope.launch {
            val result = postRepository.getAllPosts()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")

                // 必要に応じて_postの更新など
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    fun fetchAndUpdateLocation() {
        viewModelScope.launch {
            val coordinate = locationProvider.current()
            _locationState.value = LocationState(coordinate, Status.LOADED)
        }
    }
}



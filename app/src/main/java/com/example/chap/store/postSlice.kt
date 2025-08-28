package com.example.chap.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Postモデル（必要に応じてフィールド調整）
data class Post(
    val id: Int,
    val userId: Int?,
    val content: String,
    val category: String,
    val tags: List<String>,
    val coordinate: Coordinate?,
    val like: Int,
    val createdTime: String?,
    val updatedAt: String?,
    val visible: Boolean,
    val valid: Boolean
)

data class Coordinate(val lat: Double, val lng: Double)

// Redux slice相当の状態
data class PostsState(
    val items: List<Post> = emptyList(),
    val loadingFetch: Boolean = false,
    val loadingCreate: Boolean = false,
    val loadingUpdate: Boolean = false,
    val loadingDelete: Boolean = false,
    val errorFetch: String? = null,
    val errorCreate: String? = null,
    val errorUpdate: String? = null,
    val errorDelete: String? = null
)

// 位置検索用リクエスト
data class AroundRequest(val lat: Double, val lng: Double)

// Repositoryインターフェース（API層を抽象化）
interface PostsRepository {
    suspend fun fetchPosts(): List<Post>
    suspend fun fetchAround(req: AroundRequest): List<Post>
    suspend fun create(post: PostCreateRequest): Post
    suspend fun fetchPost(id: Int): Post
    suspend fun update(id: Int, patch: Map<String, Any?>): Post
    suspend fun delete(id: Int)
}

// 作成用リクエスト (id / userId / createdTime / updatedAt はサーバー付与想定)
data class PostCreateRequest(
    val content: String,
    val category: String,
    val tags: List<String>,
    val coordinate: Coordinate?,
    val like: Int = 0,
    val visible: Boolean,
    val valid: Boolean
)

class PostsViewModel(
    private val repo: PostsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostsState())
    val state: StateFlow<PostsState> = _state.asStateFlow()

    fun clearErrors() {
        _state.update {
            it.copy(
                errorFetch = null,
                errorCreate = null,
                errorUpdate = null,
                errorDelete = null
            )
        }
    }

    fun fetchPosts() = viewModelScope.launch {
        _state.update { it.copy(loadingFetch = true, errorFetch = null) }
        runCatching { repo.fetchPosts() }
            .onSuccess { list ->
                _state.update { it.copy(loadingFetch = false, items = list) }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingFetch = false, errorFetch = e.message ?: "投稿の取得に失敗しました") }
            }
    }

    fun fetchAround(lat: Double, lng: Double) = viewModelScope.launch {
        _state.update { it.copy(loadingFetch = true, errorFetch = null) }
        runCatching { repo.fetchAround(AroundRequest(lat, lng)) }
            .onSuccess { list ->
                _state.update { it.copy(loadingFetch = false, items = list) }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingFetch = false, errorFetch = e.message ?: "投稿の取得に失敗しました") }
            }
    }

    fun createPost(req: PostCreateRequest) = viewModelScope.launch {
        _state.update { it.copy(loadingCreate = true, errorCreate = null) }
        runCatching { repo.create(req) }
            .onSuccess { created ->
                _state.update { it.copy(loadingCreate = false, items = listOf(created) + it.items) }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingCreate = false, errorCreate = e.message ?: "投稿の作成に失敗しました") }
            }
    }

    fun fetchPost(id: Int) = viewModelScope.launch {
        _state.update { it.copy(loadingFetch = true, errorFetch = null) }
        runCatching { repo.fetchPost(id) }
            .onSuccess { post ->
                _state.update {
                    val idx = it.items.indexOfFirst { p -> p.id == post.id }
                    val newList = if (idx >= 0) it.items.toMutableList().apply { this[idx] = post } else it.items + post
                    it.copy(loadingFetch = false, items = newList)
                }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingFetch = false, errorFetch = e.message ?: "投稿の取得に失敗しました") }
            }
    }

    fun updatePost(id: Int, patch: Map<String, Any?>) = viewModelScope.launch {
        _state.update { it.copy(loadingUpdate = true, errorUpdate = null) }
        runCatching { repo.update(id, patch) }
            .onSuccess { updated ->
                _state.update {
                    val idx = it.items.indexOfFirst { p -> p.id == id }
                    if (idx >= 0) {
                        val list = it.items.toMutableList()
                        list[idx] = updated
                        it.copy(loadingUpdate = false, items = list)
                    } else it.copy(loadingUpdate = false)
                }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingUpdate = false, errorUpdate = e.message ?: "投稿の更新に失敗しました") }
            }
    }

    fun deletePost(id: Int) = viewModelScope.launch {
        _state.update { it.copy(loadingDelete = true, errorDelete = null) }
        runCatching { repo.delete(id) }
            .onSuccess {
                _state.update { st ->
                    st.copy(
                        loadingDelete = false,
                        items = st.items.filterNot { it.id == id }
                    )
                }
            }
            .onFailure { e ->
                _state.update { it.copy(loadingDelete = false, errorDelete = e.message ?: "投稿の削除に失敗しました") }
            }
    }
}
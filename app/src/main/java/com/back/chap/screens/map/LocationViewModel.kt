package com.back.chap.screens.map
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.models.Event
import com.back.chap.models.LocationState
import com.back.chap.models.Thread
import com.back.chap.models.Post
import com.back.chap.models.PostCreateRequest
import com.back.chap.models.Spot
import com.back.chap.models.Status
import com.back.chap.location.LocationProvider
import com.back.chap.models.SpotCreateRequest
import com.back.chap.repository.EventRepositoryImpl
import com.back.chap.repository.MapRepositoryImpl
import com.back.chap.repository.PostRepositoryImpl
import com.back.chap.repository.ThreadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val mapRepository: MapRepositoryImpl,
    private val postRepository: PostRepositoryImpl,
    private val threadRepository: ThreadRepositoryImpl,
    private val eventRepository: EventRepositoryImpl,
    private val locationProvider: LocationProvider,
    ): ViewModel() {

    private val _locationState = MutableStateFlow(LocationState(null, Status.LOADING))
    val locationState: StateFlow<LocationState> = _locationState

    init {
        fetchAndUpdateLocation()
    }

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _spots = MutableStateFlow<List<Spot>>(emptyList())
    val spots: StateFlow<List<Spot>> = _spots

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchAndUpdateLocation() {
        viewModelScope.launch {
            val coordinate = locationProvider.current()
            _locationState.value = LocationState(coordinate, Status.LOADED)
        }
    }

    fun getSpots(): List<Spot> {
        return mapRepository.spots.value
    }

    fun getPosts(): List<Post> {
        return postRepository.posts.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: PostCreateRequest) {
        viewModelScope.launch {
            val result = postRepository.createPost(post)
            result.onSuccess { created ->
                _posts.value = listOf(created) + _posts.value.filterNot { it.id == created.id }
            }.onFailure { e ->
                println("[PostViewModel] 投稿エラー: ${'$'}{e.message}")
                e.printStackTrace()
            }
        }
    }

    fun getThreads(): List<Thread> {
        return threadRepository.threads.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createThread(thread: PostCreateRequest) {
        viewModelScope.launch {
            val result = threadRepository.createThread(thread)
            result.onSuccess { created ->
                // 作成レスポンスに詳細が含まれないため、一覧を即時再取得してUIを更新
                threadRepository.getAllThreads().onSuccess { list ->
                    _threads.value = list.sortedByDescending { it.createdAt }
                }
            }.onFailure { e ->
                println("[ThreadViewModel] 作成エラー: ${'$'}{e.message}")
            }
        }
    }

    fun getEvents(): List<Event> {
        return eventRepository.events.value
    }

    fun createEvent(event: PostCreateRequest) {
        viewModelScope.launch {
            val result = eventRepository.createEvent(event)
            result.onSuccess { created ->
                _events.value = listOf(created) + _events.value.filterNot { it.id == created.id }
            }.onFailure { _ ->
                // TODO: handle error state
            }
        }
    }


    fun createSpot(spot: SpotCreateRequest) {
        viewModelScope.launch {
            val result = mapRepository.createSpot(spot)
            result.onSuccess { created ->
                _spots.value = listOf(created) + _spots.value.filterNot { it.id == created.id }
            }.onFailure { e ->
                println("[LocationViewModel] createSpot failed: ${'$'}{e.message}")
            }
        }
    }
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
            mapRepository.getAllSpots().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _spots.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _spots.value = merged.sortedByDescending { it.createdAt }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            postRepository.getAllPosts().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _posts.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _posts.value = merged.sortedByDescending { it.createdAt }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            threadRepository.getAllThreads().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _threads.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _threads.value = merged.sortedByDescending { it.createdAt }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            eventRepository.getAllEvents().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _events.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _events.value = merged.sortedByDescending { it.createdAt }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }
}

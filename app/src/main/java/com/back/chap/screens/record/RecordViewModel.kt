package com.back.chap.screens.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.location.LocationProvider
import com.back.chap.models.Event
import com.back.chap.models.Post
import com.back.chap.models.Spot
import com.back.chap.models.Thread
import com.back.chap.models.User
import com.back.chap.repository.EventRepositoryImpl
import com.back.chap.repository.MapRepositoryImpl
import com.back.chap.repository.PostRepositoryImpl
import com.back.chap.repository.ThreadRepositoryImpl
import com.back.chap.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val threadRepository: ThreadRepositoryImpl,
    private val eventRepository: EventRepositoryImpl,
    private val mapRepository : MapRepositoryImpl,
    private val locationProvider: LocationProvider,
    private val userRepository: UserRepositoryImpl,
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    private val _threads = MutableStateFlow<List<com.back.chap.models.Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _spots = MutableStateFlow<List<Spot>>(emptyList())
    val spots: StateFlow<List<Spot>> = _spots

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _ownerPhotoUrl = MutableStateFlow<String>("")
    val ownerPhotoUrl: StateFlow<String> = _ownerPhotoUrl

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage


        fun load() {
            if (_isLoading.value) return
            viewModelScope.launch {
                _isLoading.value = true
                // load current user info first
                runCatching { userRepository.getCurrentUser() }.onSuccess { user ->
                    _currentUser.value = user
                    _currentUserId.value = user?.id
                }
                mapRepository.getAllSpots().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _spots.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _spots.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }

                val coord = locationProvider.current()
                if (coord != null) {
                    postRepository.getAllPosts().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _posts.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _posts.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }
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

        fun refreshCurrentUser() {
            viewModelScope.launch {
                runCatching { userRepository.getCurrentUser() }.onSuccess { user ->
                    _currentUser.value = user
                    _currentUserId.value = user?.id
                }
            }
        }
}

package com.example.chap.screens.timeline

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.models.Event
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val postRepository : PostRepositoryImpl,
    private val threadRepository : ThreadRepositoryImpl,
    private val eventRepository : EventRepositoryImpl
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true

            postRepository.getAllPosts().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _posts.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _posts.value = merged.sortedByDescending { it.created_at }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            threadRepository.getAllThreads().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _threads.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _threads.value = merged.sortedByDescending { it.created_at }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            eventRepository.getAllEvents().onSuccess { list ->
                // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                val current = _events.value.associateBy { it.id }
                val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                _events.value = merged.sortedByDescending { it.created_at }
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }
}
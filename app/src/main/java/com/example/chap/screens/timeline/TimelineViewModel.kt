package com.example.chap.screens.timeline

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.example.chap.models.Event
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
}
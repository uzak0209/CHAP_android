package com.example.chap.API

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Event
import com.example.chap.Models.PostCreateRequest
import com.example.chap.domain.repository.EventRepositoryImpl
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepository: EventRepositoryImpl
) : ViewModel() {
    fun getEvents(): List<Event> = eventRepository.events.value

    fun getAllEvents() {
        viewModelScope.launch {
            val result = eventRepository.getAll()
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                // エラー処理
            }
        }
    }

    fun createEvent(event: PostCreateRequest) {
        viewModelScope.launch {
            val result = eventRepository.create(event)
            result.onSuccess { response ->
                // 投稿成功時の処理
            }.onFailure { e ->
                // エラー処理
            }
        }
    }
}

class EventViewModelFactory(private val eventRepository: EventRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(eventRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.chap.screens.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.models.Event
import com.example.chap.models.PostCreateRequest
import com.example.chap.repository.EventRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class EventViewModel(
    private val eventRepository: EventRepositoryImpl
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

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
                try {
                    val json = JSONObject(response)
                    val created = Event(
                        id = json.optLong("id", 0L),
                        type = json.optString("type", ""),
                        created_at = json.optString("created_at", ""),
                        updated_at = json.optString("updated_at", ""),
                        deleted_at = if (json.isNull("deleted_at")) null else json.optString("deleted_at"),
                        user_id = json.optString("user_id", ""),
                        username = json.optString("username", ""),
                        coordinate = com.example.chap.models.Coordinate(
                            lat = json.optJSONObject("coordinate")?.optDouble("lat", 0.0) ?: 0.0,
                            lng = json.optJSONObject("coordinate")?.optDouble("lng", 0.0) ?: 0.0,
                        ),
                        content = json.optString("content", ""),
                        category = json.optString("category", ""),
                        valid = json.optBoolean("valid", true),
                        like = json.optInt("like", 0),
                        tags = emptyList() // backend が tags 配列を返すなら parse へ拡張
                    )
                    _events.value = (listOf(created) + _events.value.filterNot { it.id == created.id })
                    println("[PostViewModel] Post added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("[PostViewModel] Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                // エラー処理
            }
        }
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            eventRepository.getAll().onSuccess { list ->
                _events.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()
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

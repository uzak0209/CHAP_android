package com.example.chap.screens.thread

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Thread
import com.example.chap.repository.ThreadRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


class ThreadViewModel(
    private val threadRepository: ThreadRepositoryImpl
) : ViewModel() {

    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads

    fun getThreads(): List<Thread> {
        return threadRepository.threads.value
    }
    fun getAllThreads() {
        viewModelScope.launch {
            val result = threadRepository.getAll()
            result.onSuccess { response ->
                println("スレッド一覧取得成功: $response")
                // _threadsの更新はRepository側で行われる
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    // Added helper to fetch a thread by id from current cache
    fun getThreadById(id: Long): Thread? = threadRepository.threads.value.firstOrNull { it.id == id }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createThread(thread: PostCreateRequest) {
        viewModelScope.launch {
            val result = threadRepository.create(thread)
            result.onSuccess { response ->
                println("スレッド作成成功: $response")
                try {
                    val json = JSONObject(response)
                    val created = Thread(
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
                    _threads.value = (listOf(created) + _threads.value.filterNot { it.id == created.id })
                    println("[PostViewModel] Post added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("[PostViewModel] Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun load() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            threadRepository.getAll().onSuccess { list ->
                _threads.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()
}

class ThreadViewModelFactory(private val threadRepository: ThreadRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThreadViewModel(threadRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

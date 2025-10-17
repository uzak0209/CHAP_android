package com.back.chap.screens.thread

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.models.Thread
import com.back.chap.repository.ThreadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    private val threadRepository: ThreadRepositoryImpl

) : ViewModel() {
    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads

    fun getThreads(): List<Thread> {
        return threadRepository.threads.value
    }
    fun getAllThreads() {
        viewModelScope.launch {
            val result = threadRepository.getAllThreads()
            result.onSuccess { response ->
                println("スレッド一覧取得成功: $response")
                // _threadsの更新はRepository側で行われる
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
            threadRepository.getAllThreads().onSuccess { list ->
                _threads.value = list
            }.onFailure {
                // TODO: error handling (log/report)
            }
            _isLoading.value = false
        }
    }

    fun refresh() = load()
}


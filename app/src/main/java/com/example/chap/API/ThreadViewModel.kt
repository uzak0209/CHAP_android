package com.example.chap.API

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.PostCreateRequest
import com.example.chap.Models.Thread
import com.example.chap.domain.repository.ThreadRepositoryImpl
import kotlinx.coroutines.launch

class ThreadViewModel(
    private val threadRepository: ThreadRepositoryImpl
) : ViewModel() {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createThread(thread: PostCreateRequest) {
        viewModelScope.launch {
            val result = threadRepository.create(thread)
            result.onSuccess { response ->
                println("スレッド作成成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }
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

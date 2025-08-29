package com.example.chap.API


// ViewModel で API 呼び出し処理をRepositoryに移譲
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

object LoginViewModel: ViewModel() {
    private val authRepository = AuthRepositoryImpl()
    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            val result = authRepository.login(email, password, context)
            result.onSuccess { response ->
                println("ログイン成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String, displayName: String, context: Context) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, displayName, context)
            result.onSuccess { response ->
                println(mapOf("email" to email, "password" to password, "name" to displayName))
                println("新規登録成功: $response")
            }.onFailure { e ->
                Log.e("LoginViewModel", "新規登録エラー", e)
            }
        }
    }
}
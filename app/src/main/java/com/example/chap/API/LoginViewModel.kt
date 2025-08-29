package com.example.chap.API


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.domain.repository.AuthRepository
import kotlinx.coroutines.launch

// ViewModel で API 呼び出し処理をRepositoryに移譲
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.onSuccess { response ->
                // TODO: 成功したら画面遷移や保存処理
                println("ログイン成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, displayName)
            result.onSuccess { response ->
                println(mapOf("email" to email, "password" to password, "name" to displayName))
                println("新規登録成功: $response")
            }.onFailure { e ->
                Log.e("LoginViewModel", "新規登録エラー", e)
            }
        }
    }
}
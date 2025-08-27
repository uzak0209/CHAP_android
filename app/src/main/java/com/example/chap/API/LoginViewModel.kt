package com.example.chap.API

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel で API 呼び出し処理をまとめる
class LoginViewModel : ViewModel() {
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.request(
                    url = ApiEndpoints.Auth.LOGIN,
                    method = "POST",
                    body = mapOf("email" to email, "password" to password)
                )
                // TODO: 成功したら画面遷移や保存処理
                println("ログイン成功: $response")
            } catch (e: Exception) {

                println("エラー: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String,displayName: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.request(
                    url = ApiEndpoints.Auth.REGISTER,
                    method = "POST",
                    body = mapOf("email" to email, "password" to password,"name" to displayName)
                )
                println(mapOf("email" to email, "password" to password,"name" to displayName))
                println("新規登録成功: $response")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "新規登録エラー", e)
            }
        }
    }

    companion object
}
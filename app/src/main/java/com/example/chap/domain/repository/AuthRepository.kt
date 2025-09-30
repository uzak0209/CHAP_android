package com.example.chap.domain.repository

import android.content.Context
import com.example.chap.auth.TokenManager
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import org.json.JSONObject

interface AuthRepository {
    suspend fun login(email: String, password: String, context: Context): Result<String>
    suspend fun register(email: String, password: String, displayName: String, context: Context): Result<String>
}
class AuthRepositoryImpl : AuthRepository {
    // ViewModel で API 呼び出し処理をまとめる
    override suspend fun login(email: String, password: String, context: Context): Result<String> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Auth.LOGIN,
                method = "POST",
                body = mapOf("email" to email, "password" to password)
            )
            val json = JSONObject(response ?: "")
            val token = json.optString("token", "")
            if (token.isNotBlank()) {
                TokenManager(context).saveToken(token)
                ApiClient.token = token // 即時反映
            }
            Result.success(response ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun register(email: String, password: String, displayName: String, context: Context): Result<String> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Auth.REGISTER,
                method = "POST",
                body = mapOf("email" to email, "password" to password, "name" to displayName)
            )
            val json = JSONObject(response ?: "")
            val token = json.optString("token", "")
            if (token.isNotBlank()) {
                TokenManager(context).saveToken(token)
                ApiClient.token = token // 即時反映
            }
            Result.success(response ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




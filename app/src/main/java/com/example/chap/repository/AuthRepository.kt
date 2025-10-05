package com.example.chap.repository

import android.content.Context
import com.example.chap.auth.TokenManager
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import org.json.JSONObject

interface AuthRepository {
    suspend fun login(email: String, password: String, context: Context): Result<String>
    suspend fun register(email: String, password: String, displayName: String, context: Context): Result<String>

    suspend fun logOut(context: Context)
}





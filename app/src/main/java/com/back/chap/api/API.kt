package com.back.chap.api

import android.annotation.SuppressLint
import com.back.chap.AppContextHolder
import com.back.chap.auth.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

// 1. APIエンドポイント定義（TypeScriptのAPI_ENDPOINTS風）
object ApiEndpoints {
//    const val BASE = "http://10.62.20.252:8081"
    const val BASE = "https://api.chap-app.jp"
    object Auth {
        const val LOGIN = "$BASE/api/v1/auth/signin"
        const val REGISTER = "$BASE/api/v1/auth/signup"
        const val GOOGLE = "$BASE/api/v1/auth/google"
        const val LOGOUT = "$BASE/api/v1/auth/logout"
        const val VERIFY = "$BASE/api/v1/users/me"
    }
    object Users {
        fun get(id: String) = "$BASE/api/v1/user/$id"
        fun edit(id: String) = "$BASE/api/v1/edit/user/$id"
        const val EDIT = "$BASE/api/v1/users/edit"
    }
    object Threads {
        const val LIST = "$BASE/api/v1/threads"
        fun get(id: String) = "$BASE/api/v1/thread/$id"
        fun details(id: String) = "$BASE/api/v1/thread/$id/details"
        fun reply(id: String) = "$BASE/api/v1/thread/$id/reply"
        const val CREATE = "$BASE/api/v1/threads/create"
        fun update(ts: Long) = "$BASE/api/v1/update/thread/$ts"
        fun edit(id: String) = "$BASE/api/v1/edit/thread/$id"
        fun delete(id: String) = "$BASE/api/v1/delete/thread/$id"
    }
    object Posts {
        const val LIST = "$BASE/api/v1/posts"
        fun get(id: String) = "$BASE/api/v1/post/$id"
        const val CREATE = "$BASE/api/v1/posts/create"
        fun update(ts: Long) = "$BASE/api/v1/update/post/$ts"
        fun edit(id: String) = "$BASE/api/v1/edit/post/$id"
        fun delete(id: String) = "$BASE/api/v1/delete/post/$id"
    }
    object Events {
        const val LIST = "$BASE/api/v1/events"
        fun get(id: String) = "$BASE/api/v1/event/$id"
        const val CREATE = "$BASE/api/v1/events/create"
        fun update(ts: Long) = "$BASE/api/v1/update/event/$ts"
        fun edit(id: String) = "$BASE/api/v1/edit/event/$id"
        fun delete(id: String) = "$BASE/api/v1/delete/event/$id"
    }
    object Comments {
        fun getCommentsByThread(id: String) = "$BASE/api/v1/comments/$id"
        const val CREATE = "$BASE/api/v1/comments/create"
    }
    object Spots {
        const val LIST = "$BASE/api/v1/spots"
        fun get(id: String) = "$BASE/api/v1/spot/$id"
        const val CREATE = "$BASE/api/v1/spots/create"
        fun update(ts: Long) = "$BASE/api/v1/update/spot/$ts"
        fun edit(id: String) = "$BASE/api/v1/edit/spot/$id"
        fun delete(id: String) = "$BASE/api/v1/delete/spot/$id"
    }
    object Image{
        const val COMPRESSION = "image.chap-app.jp/stage/compression"
        const val GETUPLOADURL = "$BASE/api/v1/images/upload"
    }
}

// 2. 共通APIクライアント（apiClient風）
object ApiClient {
    private val client = OkHttpClient()
    var token: String? = null
    private val gson = Gson()
    @SuppressLint("StaticFieldLeak")
    private val tokenManager= TokenManager(AppContextHolder.appContext)

    suspend fun request(
        url: String,
        method: String = "GET",
        body: Map<String, Any>? = null
    ): String? = withContext(Dispatchers.IO) {

        token=tokenManager.getToken()
        val builder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }

        val requestBody = body?.let { 
            val jsonString = gson.toJson(it)
            println("[ApiClient] Request to $url")
            println("[ApiClient] Request body: $jsonString")
            jsonString.toRequestBody("application/json".toMediaType()) 
        }
        println("[ApiClient] Token: ${token?.take(20)}...")
        builder.method(method, if (method.uppercase() == "GET") null else requestBody)
        
        val response = client.newCall(builder.build()).execute()
        val responseBody = response.body.string()
        
        println("[ApiClient] Response code: ${response.code}")
        println("[ApiClient] Response body: $responseBody")
        
        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code}: $responseBody")
        }
        
        responseBody

    }
}
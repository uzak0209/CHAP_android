package com.example.chap.API

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter

object PostViewModel: ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: MutableStateFlow<List<Post>> = _posts

    fun getAllPosts(){
        viewModelScope.launch {
            try {
                val response = ApiClient.request(
                    url = ApiEndpoints.Posts.LIST,
                    method = "POST",
                    body = mapOf("lat" to LocationViewModel.location?.lat.toString(), "lng" to LocationViewModel.location?.lng.toString())
                )
                println("ログイン成功: $response")
            } catch (e: Exception) {
                println("エラー: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: Post) {
        viewModelScope.launch {
            try {
                // 現在地を取得（null の場合は空マップ）
                val coordinateMap = LocationViewModel.location?.let { loc ->
                    mapOf(
                        "lat" to loc.lat.toString(),
                        "lng" to loc.lng.toString()
                    )
                } ?: emptyMap()

                val formatted = getCurrentTimeISO()
                val requestBody = mapOf(
                    "category" to post.category,
                    "content" to post.content,
                    "coordinate" to coordinateMap,
                    "created_at" to formatted,
                    "like" to "0",
                    "tags" to post.tags,
                    "type" to "post",
                    "valid" to "true"
                )

                // API 呼び出し
                val response = ApiClient.request(
                    url = ApiEndpoints.Posts.CREATE,
                    method = "POST",
                    body = requestBody
                )
                println("投稿成功: $response")
            } catch (e: Exception) {
                println("エラー: ${e.message}")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentTimeISO(): String {
    val now = Instant.now()  // 現在時刻をUTCで取得
    return DateTimeFormatter.ISO_INSTANT.format(now)  // "2025-08-27T11:54:57.238Z"
}
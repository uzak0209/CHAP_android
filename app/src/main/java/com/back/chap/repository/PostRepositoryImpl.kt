package com.back.chap.repository

// API連携用の実装クラス

import android.os.Build
import androidx.annotation.RequiresApi
import com.back.chap.api.ApiClient
import com.back.chap.api.ApiEndpoints
import com.back.chap.location.LocationProvider
import com.back.chap.models.Coordinate
import com.back.chap.models.PostCreateRequest
import com.back.chap.models.Post
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter



class PostRepositoryImpl @Inject constructor(private val locationProvider: LocationProvider) : PostRepository {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts

    override suspend fun getAllPosts(): Result<List<Post>> {
        return try {
            val coordinate = locationProvider.current()
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to (coordinate?.lat ?: 0.0),
                    "lng" to (coordinate?.lng ?: 0.0),
                )
            )
            // レスポンスをパースしてPostリストに変換し、_postsにセット
            println("[PostRepository] getAllPosts raw response: ${response?.toString()?.take(300)}")
            val postList = parsePosts(response)
            println("[PostRepository] getAllPosts parsed count: ${postList.size}")
            _posts.value = postList
            Result.success(postList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // レスポンス(JSON)からList<Post>へ変換する関数（ドメインモデルにマッピング）
    private fun parsePosts(response: Any?): List<Post> {
        if (response == null) return emptyList()
        return try {
            val text = response.toString().trim()
            val jsonArray = if (text.startsWith("{")) {
                // 形: { "threads": [ ... ] }
                val obj = JSONObject(text)
                obj.optJSONArray("posts") ?: JSONArray()
            } else {
                // 形: [ ... ]
                JSONArray(text)
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                parsePostObject(obj)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Coordinateのパース
    private fun parseCoordinate(obj: JSONObject?): Coordinate {
        return if (obj == null) Coordinate(0.0, 0.0)
        else Coordinate(
            lat = obj.optDouble("lat", 0.0),
            lng = obj.optDouble("lng", 0.0)
        )
    }

    // likes のパース（API に配列がある場合）
    private fun parseLikes(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return List(array.length()) { i -> array.optString(i, "") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createPost(request: PostCreateRequest): Result<Post> {
        return try {
            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "content" to request.content,
                // サーバーはトップレベルの lat/lng を期待するためフラットに送る
                "lat" to request.coordinate.lat,
                "lng" to request.coordinate.lng,
                "createdAt" to formatted,
                "type" to "post",
                "visible" to request.visible,
                "contentType" to request.category,
                "image" to request.image
            )
            println("[PostRepository] Creating post with body: $requestBody")
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.CREATE,
                method = "POST",
                body = requestBody
            )
            val bodyStr = response ?: ""
            if (bodyStr.contains("\"error\"")) {
                println("[PostRepository] Error in response: $bodyStr")
                return Result.failure(IllegalStateException("Post create failed: $bodyStr"))
            }
            val json = when (response) {
                is String -> JSONObject(response)
                else -> JSONObject(response.toString())
            }
            val created = parsePostObject(json)
            println("[PostRepository] Post created successfully: id=${'$'}{created.id}")
            Result.success(created)
        } catch (e: Exception) {
            println("[PostRepository] Exception during post creation: ${'$'}{e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }

    private fun parsePostObject(obj: JSONObject): Post {
        val idStr = obj.optString("id", "0")
        val coordinate = if (obj.has("coordinate")) {
            parseCoordinate(obj.optJSONObject("coordinate"))
        } else {
            Coordinate(
                lat = obj.optDouble("lat", 0.0),
                lng = obj.optDouble("lng", 0.0)
            )
        }
        return Post(
            id = idStr,
            userId = obj.optString("userId", ""),
            userImage = obj.optString("userImage", ""),
            image = obj.optString("image", ""),
            createdAt = obj.optString("createdAt",  ""),
            updatedAt = obj.optString("updatedAt", ""),
            userName = obj.optString("userName", ""),
            coordinate = coordinate,
            category = obj.optString("category", ""),
            content = obj.optString("content", ""),
            likeCount = obj.optLong("likeCount", 0),
            likes = parseLikes(obj.optJSONArray("likes")),
        )
    }
}
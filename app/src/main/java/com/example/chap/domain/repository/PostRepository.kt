package com.example.chap.domain.repository

// API連携用の実装クラス

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.screens.map.LocationViewModel
import com.example.chap.models.Coordinate
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

interface PostRepository {
    suspend fun getAll(): Result<List<Post>>

    suspend fun create(
        request: PostCreateRequest
    ): Result<String>
}


class PostRepositoryImpl : PostRepository {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> get() = _posts

    override suspend fun getAll(): Result<List<Post>> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to LocationViewModel.locationState.location?.lat.toString(),
                    "lng" to LocationViewModel.locationState.location?.lng.toString()
                )
            )
            // レスポンスをパースしてPostリストに変換し、_postsにセット
            val postList = parsePosts(response)
            _posts.value = postList
            Result.success(postList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // レスポンス(JSON)からList<Post>へ変換する関数（簡易実装例）
    private fun parsePosts(response: Any?): List<Post> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                Post(
                    id = obj.optLong("id", 0L),
                    type = obj.optString("type", ""),
                    created_at = obj.optString("created_at", ""),
                    updated_at = obj.optString("updated_at", ""),
                    deleted_at = if (obj.isNull("deleted_at")) null else obj.optString("deleted_at"),
                    user_id = obj.optString("user_id", ""),
                    username = obj.optString("username", ""),
                    coordinate = parseCoordinate(obj.optJSONObject("coordinate")),
                    content = obj.optString("content", ""),
                    category = obj.optString("category", ""),
                    valid = obj.optBoolean("valid", true),
                    like = obj.optInt("like", 0),
                    tags = parseTags(obj.optJSONArray("tags")),
                )
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

    // tagsのパース
    private fun parseTags(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return List(array.length()) { i -> array.optString(i, "") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun create(request: PostCreateRequest): Result<String> {
        return try {
            val coordinateMap = mapOf(
                "lat" to request.coordinate.lat,
                "lng" to request.coordinate.lng
            )
            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "category" to request.category,
                "content" to request.content,
                "coordinate" to coordinateMap,
                "created_at" to formatted,
                "like" to 0,
                "tags" to request.tags,
                "type" to "post",
                "valid" to true
            )
            println("[PostRepository] Creating post with body: $requestBody")
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.CREATE,
                method = "POST",
                body = requestBody
            )
            println("[PostRepository] API response: $response")
            // サーバーがエラー時にも200と {"error":"..."} を返すケース対策
            val bodyStr = response ?: ""
            if (bodyStr.contains("\"error\"")) {
                println("[PostRepository] Error in response: $bodyStr")
                return Result.failure(IllegalStateException("Post create failed: $bodyStr"))
            }
            println("[PostRepository] Post created successfully")
            Result.success(response.toString())
        } catch (e: Exception) {
            println("[PostRepository] Exception during post creation: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }
}
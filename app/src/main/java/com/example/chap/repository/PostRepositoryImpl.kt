package com.example.chap.repository

// API連携用の実装クラス

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.location.LocationProvider
import com.example.chap.models.Coordinate
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Post
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
                    "lat" to (coordinate?.lat?.toString() ?: ""),
                    "lng" to (coordinate?.lng?.toString() ?: "")
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

    // レスポンス(JSON)からList<Post>へ変換する関数（ドメインモデルにマッピング）
    private fun parsePosts(response: Any?): List<Post> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
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
                "type" to "post",
                "visible" to request.visible,
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
        return Post(
            id = obj.optLong("id", 0L),
            userName = obj.optString("username", ""),
            userId = obj.optString("user_id", ""),
            userImage = obj.optString("image", ""),
            createdAt = obj.optString("created_at", ""),
            updatedAt = obj.optString("updated_at", ""),
            coordinate = parseCoordinate(obj.optJSONObject("coordinate")),
            content = obj.optString("content", ""),
            category = obj.optString("category", ""),
            likes = parseLikes(obj.optJSONArray("likes")),
            likeCount = obj.optLong("like", 0)
        )
    }
}
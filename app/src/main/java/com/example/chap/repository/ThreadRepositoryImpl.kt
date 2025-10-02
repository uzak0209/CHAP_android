package com.example.chap.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.screens.map.LocationViewModel
import com.example.chap.models.Coordinate
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Thread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

class ThreadRepositoryImpl : ThreadRepository {
    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    override val threads: StateFlow<List<Thread>> = _threads

    override suspend fun getAll(): Result<List<Thread>> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Threads.LIST,
                method = "THREAD",
                body = mapOf(
                    "lat" to LocationViewModel.locationState.location?.lat.toString(),
                    "lng" to LocationViewModel.locationState.location?.lng.toString()
                )
            )
            val threadList = parseThreads(response)
            _threads.value = threadList
            Result.success(threadList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // レスポンス(JSON)からList<Thread>へ変換する関数
    private fun parseThreads(response: Any?): List<Thread> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                Thread(
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
                    tags = parseTags(obj.optJSONArray("tags"))
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
    override suspend fun create(thread: PostCreateRequest): Result<String> {
        return try {
            val coordinateMap = mapOf(
                "lat" to thread.coordinate.lat,
                "lng" to thread.coordinate.lng
            )
            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "category" to thread.category,
                "content" to thread.content,
                "coordinate" to coordinateMap,
                "created_at" to formatted,
                "like" to 0,
                "tags" to thread.tags,
                "type" to "thread",
                "valid" to true
            )
            println("[ThreadRepository] Creating thread with body: $requestBody")

            val response = ApiClient.request(
                url = ApiEndpoints.Threads.CREATE,
                method = "POST",
                body = requestBody
            )
            Result.success(response.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getThreadById(id: String): Result<Thread> {
        try {
            val url = ApiEndpoints.Threads.details(id)
            val response = ApiClient.request(url)

            if (response != null) {
                val threadObject = JSONObject(response).getJSONObject("thread")
                val thread = Thread(
                    id = threadObject.optLong("id", 0L),
                    type = threadObject.optString("type", ""),
                    created_at = threadObject.optString("created_at", ""),
                    updated_at = threadObject.optString("updated_at", ""),
                    deleted_at = if (threadObject.isNull("deleted_at")) null else threadObject.optString(
                        "deleted_at"
                    ),
                    user_id = threadObject.optString("user_id", ""),
                    username = threadObject.optString("username", ""),
                    coordinate = parseCoordinate(threadObject.optJSONObject("coordinate")),
                    content = threadObject.optString("content", ""),
                    category = threadObject.optString("category", ""),
                    valid = threadObject.optBoolean("valid", true),
                    like = threadObject.optInt("like", 0),
                    tags = parseTags(threadObject.optJSONArray("tags"))
                )
                return Result.success(thread)
            } else {
               return  Result.failure(Exception("Failed to fetch thread details"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }

}

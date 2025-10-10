package com.example.chap.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.location.LocationProvider
import com.example.chap.models.Coordinate
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Thread
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

class ThreadRepositoryImpl @Inject constructor(private val locationProvider: LocationProvider) : ThreadRepository {
    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    override val threads: StateFlow<List<Thread>> = _threads

    override suspend fun getAllThreads(): Result<List<Thread>> {
        return try {
            val coordinate = locationProvider.current()
            val response = ApiClient.request(
                url = ApiEndpoints.Threads.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to (coordinate?.lat?.toString() ?: ""),
                    "lng" to (coordinate?.lng?.toString() ?: "")
                )
            )
            val threadList = parseThreads(response)
            _threads.value = threadList
            Result.success(threadList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // レスポンス(JSON)からList<Thread>へ変換（ドメインモデルにマッピング）
    private fun parseThreads(response: Any?): List<Thread> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                parseThreadObject(obj)
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

    // likes のパース
    private fun parseLikes(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return List(array.length()) { i -> array.optString(i, "") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createThread(thread: PostCreateRequest): Result<Thread> {
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
                "type" to "thread",
                "visible" to thread.visible,
            )
            println("[ThreadRepository] Creating thread with body: $requestBody")

            val response = ApiClient.request(
                url = ApiEndpoints.Threads.CREATE,
                method = "POST",
                body = requestBody
            )
            val json = when (response) {
                is String -> JSONObject(response)
                else -> JSONObject(response.toString())
            }
            val created = parseThreadObject(json)
            Result.success(created)
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
                val thread = parseThreadObject(threadObject)
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

    private fun parseThreadObject(obj: JSONObject): Thread {
        return Thread(
            id = obj.optLong("id", 0L),
            userId = obj.optString("user_id", ""),
            createdAt = obj.optString("created_at", ""),
            updatedAt = obj.optString("updated_at", ""),
            userName = obj.optString("username", ""),
            coordinate = parseCoordinate(obj.optJSONObject("coordinate")),
            category = obj.optString("category", ""),
            content = obj.optString("content", ""),
            likeCount = obj.optLong("like", 0),
            likes = parseLikes(obj.optJSONArray("likes"))
        )
    }
}

package com.back.chap.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.back.chap.api.ApiClient
import com.back.chap.api.ApiEndpoints
import com.back.chap.location.LocationProvider
import com.back.chap.models.Coordinate
import com.back.chap.models.PostCreateRequest
import com.back.chap.models.Thread
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
            println("[ThreadRepository] getAllThreads url: ${ApiEndpoints.Threads.LIST}")
            val response = ApiClient.request(
                url = ApiEndpoints.Threads.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to (coordinate?.lat ?: 0.0),
                    "lng" to (coordinate?.lng ?: 0.0),
                )
            )
            println("[ThreadRepository] getAllThreads raw response: ${response?.toString()?.take(300)}")
            val threadList = parseThreads(response)
            println("[ThreadRepository] getAllThreads parsed count: ${threadList.size}")
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
            val text = response.toString().trim()
            val jsonArray = if (text.startsWith("{")) {
                // 形: { "threads": [ ... ] }
                val obj = JSONObject(text)
                obj.optJSONArray("threads") ?: JSONArray()
            } else {
                // 形: [ ... ]
                JSONArray(text)
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
            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "content" to thread.content,
                // サーバーがトップレベルの lat/lng を期待する可能性に対応
                "lat" to thread.coordinate.lat,
                "lng" to thread.coordinate.lng,
                "createdAt" to formatted,
                "visible" to thread.visible,
                "contentType" to thread.category,
                "image" to thread.image
            )
            println("[ThreadRepository] Creating thread with body: $requestBody")

            val response = ApiClient.request(
                url = ApiEndpoints.Threads.CREATE,
                method = "POST",
                body = requestBody
            )
            println("[ThreadRepository] Create thread response: ${response?.toString()?.take(500)}")
            val json = when (response) {
                is String -> JSONObject(response)
                else -> JSONObject(response.toString())
            }
            // API might return the created thread directly or wrap it in { "thread": { ... } }
            val threadObj = if (json.has("thread")) json.getJSONObject("thread") else json
            println("[ThreadRepository] Thread object for parsing: ${threadObj.toString().take(500)}")
            val created = parseThreadObject(threadObj)
            println("[ThreadRepository] Parsed thread - id: ${created.id}, image: '${created.image}'")
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
        val idStr = obj.optString("id", "0")
        val coord = if (obj.has("coordinate")) {
            parseCoordinate(obj.optJSONObject("coordinate"))
        } else {
            Coordinate(
                lat = obj.optDouble("lat", 0.0),
                lng = obj.optDouble("lng", 0.0)
            )
        }
        return Thread(
            id = idStr,
            userId = obj.optString("userId",  ""),
            userImage = obj.optString("userImage", ""),
            image = obj.optString("image", ""),
            createdAt = obj.optString("createdAt", ""),
            updatedAt = obj.optString("updatedAt", ""),
            userName = obj.optString("userName",  ""),
            coordinate = coord,
            category = obj.optString("category", ""),
            content = obj.optString("content", ""),
            likeCount = obj.optLong("likeCount", 0),
            likes = parseLikes(obj.optJSONArray("likes")),
        )
    }
}

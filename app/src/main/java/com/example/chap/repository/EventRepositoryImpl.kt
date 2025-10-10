package com.example.chap.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.location.LocationProvider
import com.example.chap.models.Coordinate
import com.example.chap.models.Event
import com.example.chap.models.PostCreateRequest
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

class EventRepositoryImpl @Inject constructor(private val locationProvider: LocationProvider) : EventRepository {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    override val events: StateFlow<List<Event>> = _events

    override suspend fun getAllEvents(): Result<List<Event>> {
        return try {
            val coordinate = locationProvider.current()
            val response = ApiClient.request(
                url = ApiEndpoints.Events.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to (coordinate?.lat?.toString() ?: ""),
                    "lng" to (coordinate?.lng?.toString() ?: "")
                )
            )

            val eventList = parseEvents(response)
            _events.value = eventList
            Result.success(eventList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    // レスポンス(JSON)からList<Event>へ変換（ドメインモデルにマッピング）
    private fun parseEvents(response: Any?): List<Event> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                parseEventObject(obj)
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
    override suspend fun createEvent(request: PostCreateRequest): Result<Event> {
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
                "type" to "event",
                "visible" to request.visible,
            )
            println("[EventRepository] Creating event with body: $requestBody")

            val response = ApiClient.request(
                url = ApiEndpoints.Events.CREATE,
                method = "POST",
                body = requestBody
            )
            val json = when (response) {
                is String -> JSONObject(response)
                else -> JSONObject(response.toString())
            }
            val created = parseEventObject(json)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }
    
    private fun parseEventObject(obj: JSONObject): Event {
        return Event(
            id = obj.optLong("id", 0L),
            createdAt = obj.optString("created_at", ""),
            updatedAt = obj.optString("updated_at", ""),
            userName = obj.optString("username", ""),
            userId = obj.optString("user_id", ""),
            coordinate = parseCoordinate(obj.optJSONObject("coordinate")),
            category = obj.optString("category", ""),
            content = obj.optString("content", ""),
            likes = parseLikes(obj.optJSONArray("likes")),
            likeCount = obj.optLong("like", 0)
        )
    }
}

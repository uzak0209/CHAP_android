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
            val payload = mapOf(
                "lat" to (coordinate?.lat ?: 0.0),
                "lng" to (coordinate?.lng ?: 0.0),
            )
            val response = ApiClient.request(
                url = ApiEndpoints.Events.LIST,
                method = "POST",
                body = payload
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
            val text = response.toString().trim()
            val jsonArray = if (text.startsWith("{")) {
                // 形: { "events": [ ... ] }
                val obj = JSONObject(text)
                obj.optJSONArray("events") ?: JSONArray()
            } else {
                // 形: [ ... ]
                JSONArray(text)
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

            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "content" to request.content,
                "title" to request.content, // mirror content as title for now
                "lat" to request.coordinate.lat,
                "lng" to request.coordinate.lng,
                "created_at" to formatted,
                "event_date" to formatted,
                "content_type" to request.category,
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
        val coordinateJson = obj.optJSONObject("coordinate")
        val coordinate = if (coordinateJson != null) {
            parseCoordinate(coordinateJson)
        } else {
            Coordinate(
                lat = obj.optDouble("lat", 0.0),
                lng = obj.optDouble("lng", 0.0)
            )
        }
        return Event(
            id = obj.optLong("id", 0L),
            createdAt = obj.optString("created_at", obj.optString("createdAt", "")),
            updatedAt = obj.optString("updated_at", obj.optString("updatedAt", "")),
            userName = obj.optString("user_name", obj.optString("username", "")),
            userId = obj.optString("user_id", obj.optString("userId", "")),
            coordinate = coordinate,
            category = obj.optString("category", ""),
            content = obj.optString("content", obj.optString("title", "")),
            likes = parseLikes(obj.optJSONArray("likes")),
            likeCount = obj.optLong("like_count", obj.optLong("like", 0)),
            userImage = obj.optString("user_image", ""),
            image = obj.optString("image", ""),
            deletedAt = obj.optString("deleted_at", ""),
            eventDate = obj.optString("event_date", ""),
            contentType = obj.optString("content_type", obj.optString("contentType", "")),
            title = obj.optString("title", ""),
            valid = obj.optBoolean("valid", true)
        )
    }
}

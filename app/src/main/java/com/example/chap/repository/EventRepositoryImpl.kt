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
    

    // レスポンス(JSON)からList<Event>へ変換する関数（簡易実装例）
    private fun parseEvents(response: Any?): List<Event> {
        if (response == null) return emptyList()
        return try {
            val jsonArray = when (response) {
                is String -> JSONArray(response)
                else -> JSONArray(response.toString())
            }
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                Event(
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
    override suspend fun createEvent(request: PostCreateRequest): Result<String> {
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
                "type" to "event",
                "valid" to true
            )
            println("[EventRepository] Creating event with body: $requestBody")

            val response = ApiClient.request(
                url = ApiEndpoints.Events.CREATE,
                method = "POST",
                body = requestBody
            )
            Result.success(response.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }
    
}

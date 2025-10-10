package com.example.chap.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.location.LocationProvider
import com.example.chap.models.Coordinate
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Spot
import com.example.chap.screens.map.LocationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class MapRepositoryImpl  @Inject constructor(private val locationProvider: LocationProvider) :MapRepository {
    private val _spots = MutableStateFlow<List<Spot>>(emptyList())
    override val spots: StateFlow<List<Spot>> get() = _spots

    override suspend fun getAllSpots(): Result<List<Spot>> {
        val coordinate = locationProvider.current()
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to (coordinate?.lat?.toString() ?:""),
                    "lng" to (coordinate?.lng?.toString() ?:"")
                )
            )
            // レスポンスをパースしてSpotリストに変換し、_postsにセット
            val spotList = parseSpots(response)
            _spots.value = spotList
            Result.success(spotList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createSpot(request: PostCreateRequest): Result<Spot> {
        return try {
            val coordinateMap = mapOf(
                "lat" to request.coordinate.lat,
                "lng" to request.coordinate.lng
            )
            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "content" to request.content,
                "coordinate" to coordinateMap,
                "created_at" to formatted,
                "type" to "spot",
                "visible" to request.visible,
            )
            println("[SpotRepository] Creating post with body: $requestBody")
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.CREATE,
                method = "POST",
                body = requestBody
            )
            println("[SpotRepository] API response: $response")
            // サーバーがエラー時にも200と {"error":"..."} を返すケース対策
            val bodyStr = response ?: ""
            if (bodyStr.contains("\"error\"")) {
                println("[SpotRepository] Error in response: $bodyStr")
                return Result.failure(IllegalStateException("Spot create failed: $bodyStr"))
            }
            val json = when (response) {
                is String -> JSONObject(response)
                else -> JSONObject(response.toString())
            }
            val created = parseSpotObject(json)
            println("[SpotRepository] Spot created successfully: id=${'$'}{created.id}")
            Result.success(created)
        } catch (e: Exception) {
            println("[SpotRepository] Exception during post creation: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

private fun parseSpots(response: Any?): List<Spot> {
    if (response == null) return emptyList()
    return try {
        val jsonArray = when (response) {
            is String -> JSONArray(response)
            else -> JSONArray(response.toString())
        }
        val all = List(jsonArray.length()) { i -> jsonArray.getJSONObject(i) }
        all
            .filter { it.optString("type", "") == "spot" }
            .map { obj -> parseSpotObject(obj) }
    } catch (e: Exception) {
        emptyList()
    }
}

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
private fun getCurrentTimeISO(): String {
    val now = Instant.now()
    return DateTimeFormatter.ISO_INSTANT.format(now)
}

private fun parseSpotObject(obj: JSONObject): Spot {
    return Spot(
        id = obj.optLong("id", 0L),
        createdAt = obj.optString("created_at", ""),
        updatedAt = obj.optString("updated_at", ""),
        userId = obj.optString("user_id", ""),
        userName = obj.optString("username", ""),
        coordinate = parseCoordinate(obj.optJSONObject("coordinate")),
        content = obj.optString("content", "")
    )
}
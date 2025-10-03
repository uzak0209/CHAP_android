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
                url = ApiEndpoints.Spots.LIST,
                method = "SPOT",
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
    override suspend fun createSpot(request: PostCreateRequest): Result<String> {
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
                "type" to "spot",
                "valid" to true
            )
            println("[SpotRepository] Creating post with body: $requestBody")
            val response = ApiClient.request(
                url = ApiEndpoints.Spots.CREATE,
                method = "SPOT",
                body = requestBody
            )
            println("[SpotRepository] API response: $response")
            // サーバーがエラー時にも200と {"error":"..."} を返すケース対策
            val bodyStr = response ?: ""
            if (bodyStr.contains("\"error\"")) {
                println("[SpotRepository] Error in response: $bodyStr")
                return Result.failure(IllegalStateException("Spot create failed: $bodyStr"))
            }
            println("[SpotRepository] Spot created successfully")
            Result.success(response.toString())
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
        List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            Spot(
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
private fun getCurrentTimeISO(): String {
    val now = Instant.now()
    return DateTimeFormatter.ISO_INSTANT.format(now)
}
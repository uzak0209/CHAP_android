package com.example.chap.screens.map
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.models.Coordinate
import com.example.chap.models.Event
import com.example.chap.models.Thread
import com.example.chap.models.Post
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Spot
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.MapRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class Status{
    LOADING,LOADED,ERROR
}
data class LocationState(
    var location: Coordinate?,
    var status: Status
)
class LocationViewModel(
    private val mapRepository: MapRepositoryImpl,
    private val postRepository: PostRepositoryImpl,
    private val threadRepository: ThreadRepositoryImpl,
    private val eventRepository: EventRepositoryImpl,
    ): ViewModel(){

    private var _locationState: LocationState = LocationState(null, Status.LOADED)
    val locationState: LocationState
        get() = _locationState

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _threads = MutableStateFlow<List<Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _spots = MutableStateFlow<List<Spot>>(emptyList())
    val spots: StateFlow<List<Spot>> = _spots

    fun updateLocation(newLocationState: LocationState) {
        _locationState = newLocationState
        println("Location updated: ${newLocationState.location}")
    }

    fun getPosts(): List<Post> {
        return postRepository.posts.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: PostCreateRequest) {
        viewModelScope.launch {
            val result = postRepository.create(post)
            result.onSuccess { response ->
                println("[PostViewModel] 投稿成功: $response")
                // レスポンスJSONをPostにパースしてStateFlowへ即時反映
                try {
                    val json = JSONObject(response)
                    val created = Post(
                        id = json.optLong("id", 0L),
                        type = json.optString("type", ""),
                        created_at = json.optString("created_at", ""),
                        updated_at = json.optString("updated_at", ""),
                        deleted_at = if (json.isNull("deleted_at")) null else json.optString("deleted_at"),
                        user_id = json.optString("user_id", ""),
                        username = json.optString("username", ""),
                        coordinate = com.example.chap.models.Coordinate(
                            lat = json.optJSONObject("coordinate")?.optDouble("lat", 0.0) ?: 0.0,
                            lng = json.optJSONObject("coordinate")?.optDouble("lng", 0.0) ?: 0.0,
                        ),
                        content = json.optString("content", ""),
                        category = json.optString("category", ""),
                        valid = json.optBoolean("valid", true),
                        like = json.optInt("like", 0),
                        tags = emptyList()
                    )
                    _posts.value = (listOf(created) + _posts.value.filterNot { it.id == created.id })
                    println("[PostViewModel] Post added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("[PostViewModel] Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                println("[PostViewModel] 投稿エラー: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun getThreads(): List<Thread> {
        return threadRepository.threads.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createThread(thread: PostCreateRequest) {
        viewModelScope.launch {
            val result = threadRepository.create(thread)
            result.onSuccess { response ->
                println("スレッド作成成功: $response")
                try {
                    val json = JSONObject(response)
                    val created = Thread(
                        id = json.optLong("id", 0L),
                        type = json.optString("type", ""),
                        created_at = json.optString("created_at", ""),
                        updated_at = json.optString("updated_at", ""),
                        deleted_at = if (json.isNull("deleted_at")) null else json.optString("deleted_at"),
                        user_id = json.optString("user_id", ""),
                        username = json.optString("username", ""),
                        coordinate = com.example.chap.models.Coordinate(
                            lat = json.optJSONObject("coordinate")?.optDouble("lat", 0.0) ?: 0.0,
                            lng = json.optJSONObject("coordinate")?.optDouble("lng", 0.0) ?: 0.0,
                        ),
                        content = json.optString("content", ""),
                        category = json.optString("category", ""),
                        valid = json.optBoolean("valid", true),
                        like = json.optInt("like", 0),
                        tags = emptyList() // backend が tags 配列を返すなら parse へ拡張
                    )
                    _threads.value = (listOf(created) + _threads.value.filterNot { it.id == created.id })
                    println(" Thread added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("Thread Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
        }
    }

    fun getEvents(): List<Event> {
        return eventRepository.events.value
    }

    fun createEvent(event: PostCreateRequest) {
        viewModelScope.launch {
            val result = eventRepository.create(event)
            result.onSuccess { response ->
                // 投稿成功時の処理
                try {
                    val json = JSONObject(response)
                    val created = Event(
                        id = json.optLong("id", 0L),
                        type = json.optString("type", ""),
                        created_at = json.optString("created_at", ""),
                        updated_at = json.optString("updated_at", ""),
                        deleted_at = if (json.isNull("deleted_at")) null else json.optString("deleted_at"),
                        user_id = json.optString("user_id", ""),
                        username = json.optString("username", ""),
                        coordinate = com.example.chap.models.Coordinate(
                            lat = json.optJSONObject("coordinate")?.optDouble("lat", 0.0) ?: 0.0,
                            lng = json.optJSONObject("coordinate")?.optDouble("lng", 0.0) ?: 0.0,
                        ),
                        content = json.optString("content", ""),
                        category = json.optString("category", ""),
                        valid = json.optBoolean("valid", true),
                        like = json.optInt("like", 0),
                        tags = emptyList()
                    )
                    _events.value = (listOf(created) + _events.value.filterNot { it.id == created.id })
                    println(" Event added to StateFlow with ID: ${created.id}")
                } catch (e: Exception) {
                    println("Event Failed to parse response JSON: ${e.message}")
                    e.printStackTrace()
                }
            }.onFailure { e ->
                // エラー処理
            }
        }
    }
}
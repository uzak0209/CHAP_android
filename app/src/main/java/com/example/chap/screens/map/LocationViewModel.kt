package com.example.chap.screens.map
import androidx.lifecycle.ViewModel
import com.example.chap.models.Coordinate
import com.example.chap.models.Event
import com.example.chap.models.Post
import com.example.chap.models.Spot
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.MapRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
}
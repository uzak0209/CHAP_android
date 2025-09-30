package com.example.chap.screens.map
import com.example.chap.models.Coordinate
enum class Status{
    LOADING,LOADED,ERROR
}
data class LocationState(
    var location: Coordinate?,
    var status: Status
)
object LocationViewModel {

    private var _locationState: LocationState = LocationState(null, Status.LOADED)
    val locationState: LocationState
        get() = _locationState

    fun updateLocation(newLocationState: LocationState) {
        _locationState = newLocationState
        println("Location updated: ${newLocationState.location}")
    }
}
package com.example.chap.API
import com.example.chap.Models.Coordinate
enum class Status{
    LOADING,LOADED,ERROR
}
class LocationState(coordinate: Coordinate?, loaded: Status) {
    var location: Coordinate? = null
    var status: Status = Status.LOADING
}

object LocationViewModel {

    private var _locationState: LocationState = LocationState(null, Status.LOADED)
    val locationState: LocationState
        get() = _locationState

    fun updateLocation(newLocationState: LocationState) {
        _locationState = newLocationState
        println("Location updated: $newLocationState")
    }
}
package com.example.chap.API
import androidx.lifecycle.ViewModel
import com.example.chap.Models.Coordinate

class LocationViewModel: ViewModel() {
    private var _location: Coordinate? = null
    val location: Coordinate?
        get() = _location

    fun updateLocation(newLocation: Coordinate) {
        _location = newLocation
        println("Location updated: $newLocation")
    }
}
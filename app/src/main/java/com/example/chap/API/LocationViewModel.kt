package com.example.chap.API
import com.example.chap.Models.Coordinate

object LocationViewModel {
    private var _location: Coordinate? = null
    val location: Coordinate?
        get() = _location

    fun updateLocation(newLocation: Coordinate) {
        _location = newLocation
        println("Location updated: $newLocation")
    }
}
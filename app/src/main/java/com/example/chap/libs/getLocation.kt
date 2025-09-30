package com.example.chap.libs

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chap.screens.map.LocationState
import com.example.chap.screens.map.LocationViewModel
import com.example.chap.screens.map.Status
import com.example.chap.models.Coordinate
import com.google.android.gms.location.LocationServices

val LOCATION_PERMISSION_REQUEST_CODE = 1000
fun GetLocation(
    context: ComponentActivity,
    requestCode: Int
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestCode
        )
    } else {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val coordinate =
                    Coordinate(location.latitude, location.longitude)
                println("location: $coordinate")
                LocationViewModel.updateLocation(LocationState(coordinate, Status.LOADED))
                println("newLocation ${LocationViewModel.locationState.location}")
            }
        }
    }
}
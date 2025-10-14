package com.example.chap.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.chap.models.Coordinate
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


val LOCATION_PERMISSION_REQUEST_CODE = 1000
class DefaultLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {
    override suspend fun current(): Coordinate? {
        //Permissionの確認
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCoroutine<android.location.Location?> { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
        return location?.let { Coordinate(it.latitude, it.longitude) }
    }
}



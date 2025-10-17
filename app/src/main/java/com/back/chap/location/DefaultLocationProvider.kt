package com.back.chap.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.back.chap.models.Coordinate
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
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
        if (location != null) {
            return Coordinate(location.latitude, location.longitude)
        }

        // lastLocation が null の場合は高精度の現在地取得を試みる
        val fresh = suspendCoroutine<android.location.Location?> { cont ->
            val cts = CancellationTokenSource()
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
        return fresh?.let { Coordinate(it.latitude, it.longitude) }
    }
}



package com.back.chap.components

import android.location.Location
import android.util.Log
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// 位置計測ユーティリティ関数群。
// Map 画面 (UserLocationMap) からも利用するため private を外し公開。

/** 2点間距離(m) */
fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val a = Location("a").apply { latitude = lat1; longitude = lon1 }
    val b = Location("b").apply { latitude = lat2; longitude = lon2 }
    return a.distanceTo(b)
}

/**
 * エミュレータ初期値を避けるために複数回最新の高精度位置を取得し、
 * 妥当な位置 (デフォルト座標から一定距離以上) が得られた時に onValid をコール。
 */
fun requestFreshLocationIfNeeded(
    tag: String,
    fused: com.google.android.gms.location.FusedLocationProviderClient,
    emulatorDefaultLat: Double,
    emulatorDefaultLon: Double,
    attempt: Int,
    maxAttempts: Int,
    setAttempt: (Int) -> Unit,
    onValid: (Location) -> Unit
) {
    if (attempt >= maxAttempts) return
    val next = attempt + 1
    setAttempt(next)
    val cts = CancellationTokenSource()
    try {
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Log.w(tag, "fresh attempt#$next null")
                    return@addOnSuccessListener
                }
                val distEmu = distanceMeters(loc.latitude, loc.longitude, emulatorDefaultLat, emulatorDefaultLon)
                Log.d(tag, "fresh attempt#$next lat=${loc.latitude} lon=${loc.longitude} acc=${loc.accuracy} distEmu=$distEmu")
                if (distEmu > 1500 || attempt == 0) {
                    onValid(loc)
                }
            }
            .addOnFailureListener { e -> Log.e(tag, "fresh attempt#$next failure: ${e.message}", e) }
    } catch (se: SecurityException) {
        Log.e(tag, "fresh location security exception: ${se.message}")
    }
}

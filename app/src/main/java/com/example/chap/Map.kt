package com.example.chap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
// import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.chap.components.distanceMeters
import com.example.chap.components.requestFreshLocationIfNeeded
import com.example.chap.components.logCurrentLocation

@Composable
fun UserLocationMap() {
    val context = LocalContext.current
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }
    var styleLoaded by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }
    val TAG = "UserLocationMap"
    // Emulator default (Googleplex) and Japan bounds
    val emulatorDefaultLat = 37.4219983
    val emulatorDefaultLon = -122.084
    val japanLatMin = 24.0
    val japanLatMax = 46.5
    val japanLonMin = 122.0
    val japanLonMax = 153.0
    var freshAttempts by remember { mutableStateOf(0) }
    val maxFreshAttempts = 5

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val hasLocationPermission = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "Permission result: $result")
        if (result.values.any { it }) {
            Log.d(TAG, "At least one location permission granted. Fetching lastLocation...")
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    Log.d(TAG, "Initial lastLocation success lat=${loc.latitude} lon=${loc.longitude} acc=${loc.accuracy}")
                    lastLocation = loc.also { logCurrentLocation(TAG, it) }
                } else {
                    Log.w(TAG, "Initial lastLocation is null")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "lastLocation failure: ${e.message}", e)
            }
        }
    }

    LaunchedEffect(hasLocationPermission, permissionRequested) {
        if (!hasLocationPermission && !permissionRequested) {
            Log.d(TAG, "Requesting permissions...")
            permissionRequested = true
            launcher.launch(permissions)
        } else if (hasLocationPermission) {
            Log.d(TAG, "Permissions already granted. Fetching lastLocation...")
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    Log.d(TAG, "lastLocation success lat=${loc.latitude} lon=${loc.longitude} acc=${loc.accuracy}")
                    lastLocation = loc.also { logCurrentLocation(TAG, it) }
                } else {
                    Log.w(TAG, "lastLocation returned null (retryCount=$retryCount)")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "lastLocation failure: ${e.message}", e)
            }
            // 追加: 高精度の最新位置を取得（キャッシュではなく）
            try {
                val cts = CancellationTokenSource()
                fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { fresh ->
                        if (fresh != null) {
                            Log.d(TAG, "getCurrentLocation lat=${fresh.latitude} lon=${fresh.longitude} acc=${fresh.accuracy}")
                            val previous = lastLocation
                            val previousWasEmuDefault = previous?.let { distanceMeters(it.latitude, it.longitude, emulatorDefaultLat, emulatorDefaultLon) < 80 } ?: true
                            val freshIsDifferent = distanceMeters(fresh.latitude, fresh.longitude, emulatorDefaultLat, emulatorDefaultLon) > 1000
                            if (previousWasEmuDefault && freshIsDifferent) {
                                Log.d(TAG, "Replacing emulator default with fresh real location")
                                lastLocation = fresh.also { logCurrentLocation(TAG, it) }
                            } else if (previous == null) {
                                lastLocation = fresh.also { logCurrentLocation(TAG, it) }
                            }
                        } else {
                            Log.w(TAG, "getCurrentLocation returned null")
                        }
                    }
                    .addOnFailureListener { e -> Log.e(TAG, "getCurrentLocation failure: ${e.message}", e) }
            } catch (se: SecurityException) {
                Log.e(TAG, "getCurrentLocation security exception: ${se.message}")
            }
        }
    }

    // 初期ズーム（起動直後位置未確定時）※値を調整すると初期表示の拡大度合いを変更可能
    val initialZoom = 30.0
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(initialZoom)
            center(Point.fromLngLat(-98.0, 39.5)) // 初期中心（後で位置取得後に更新）
            pitch(0.0)
            bearing(0.0)
        }
    }

    // スタイル読み込み後に一度だけ初期カメラ(またはユーザー位置)へズームするためのフラグ
    var cameraInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(lastLocation) {
        if (lastLocation != null) {
            val loc = lastLocation!!
            val isEmuDefault = distanceMeters(loc.latitude, loc.longitude, emulatorDefaultLat, emulatorDefaultLon) < 80
            if (!isEmuDefault) {
                viewportState.setCameraOptions {
                    center(Point.fromLngLat(loc.longitude, loc.latitude))
                    lastLocation?.let {
                        zoom(16.5)
                    }
                }
            } else if (freshAttempts < maxFreshAttempts) {
                Log.d(TAG, "Still emulator default -> fresh request (#$freshAttempts)")
                requestFreshLocationIfNeeded(
                    tag = TAG,
                    fused = fused,
                    emulatorDefaultLat = emulatorDefaultLat,
                    emulatorDefaultLon = emulatorDefaultLon,
                    attempt = freshAttempts,
                    maxAttempts = maxFreshAttempts,
                    setAttempt = { freshAttempts = it },
                    onValid = { l -> lastLocation = l.also { logCurrentLocation(TAG, it) } }
                )
            }
        } else if (hasLocationPermission && retryCount == 0) {
            retryCount++
            Log.d(TAG, "Scheduling one-time retry for lastLocation in 1500ms")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                delay(1500)
                fused.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        Log.d(TAG, "Retry lastLocation success lat=${loc.latitude} lon=${loc.longitude} acc=${loc.accuracy}")
                        lastLocation = loc.also { logCurrentLocation(TAG, it) }
                    } else {
                        Log.w(TAG, "Retry lastLocation still null")
                    }
                }
            }
        }
    }

    // スタイル読み込み完了後 & カメラ未初期化の場合に一度だけズームを適用
    LaunchedEffect(styleLoaded, lastLocation, cameraInitialized) {
        if (styleLoaded && !cameraInitialized) {
            viewportState.setCameraOptions {
                if (lastLocation != null) {
                    center(Point.fromLngLat(lastLocation!!.longitude, lastLocation!!.latitude))
                    zoom(16.5)
                } else {
                    zoom(initialZoom) // 位置未取得なら初期ズーム反映
                }
            }
            cameraInitialized = true
            Log.d(TAG, "Initial camera applied (styleLoaded)")
        }
    }

    // 2D / 3D 切替用フラグ
    var is3D by remember { mutableStateOf(false) }

    // 背景色 (スタイル未読込時の黒画面緩和)
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            // 正しい DSL でスタイル適用 (STANDARD か streets 等)
            style = { (Style.STANDARD) }
        ) {
            MapEffect(Unit) { mapView ->
                // まだ style が null なら読み込み指示 (一度だけ)
                if (mapView.getMapboxMap().style == null && !styleLoaded) {
                    mapView.getMapboxMap().loadStyleUri(Style.STANDARD) { _ ->
                        styleLoaded = true
                    }
                } else if (mapView.getMapboxMap().style != null) {
                    styleLoaded = true
                }
            }
            MapEffect(lastLocation) { mapView ->
                val plugin = mapView.location
                plugin.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }
                if (lastLocation == null) {
                    Log.d(TAG, "Location plugin enabled but lastLocation is null yet")
                }

            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            onClick = {
                is3D = !is3D
                toggleDimension(viewportState, is3D, lastLocation)
            }
        ) {
            Text(if (is3D) "2D" else "3D")
        }
        if (!styleLoaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("地図スタイル読み込み中…", color = Color.DarkGray)
            }
        }
    }
}
private fun toggleDimension(
    viewportState: com.mapbox.maps.extension.compose.animation.viewport.MapViewportState,
    is3D: Boolean,
    lastLocation: Location?
) {
    // カメラオプション更新: ピッチとベアリングをトグル
    viewportState.setCameraOptions {
        pitch(if (is3D) 60.0 else 0.0)
        bearing(if (is3D) 45.0 else 0.0)
        // 位置が取れている場合はそこへフォーカス (未取得なら現状維持)
        lastLocation?.let {
            center(Point.fromLngLat(it.longitude, it.latitude))
            if (is3D) {
                // 3D 表示時少しズームアップ
                zoom(16.5)
            }
        }
    }
}


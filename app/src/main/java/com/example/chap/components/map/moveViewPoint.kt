package com.example.chap.components.map

import com.example.chap.models.Coordinate
import com.example.chap.screens.map.LocationViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import kotlinx.coroutines.launch

fun moveViewPoint(
    viewportState: MapViewportState,
    scope: kotlinx.coroutines.CoroutineScope,
    location: Coordinate?,
) {

    if (location == null) {
        println("[ReturnMyLocation] 現在地が取得できていません")
        return
    }

    println("[ReturnMyLocation] カメラを現在地に移動: lat=${location.lat}, lng=${location.lng}")

    // カメラを現在地にアニメーションで移動
    scope.launch {
        viewportState.flyTo(
            cameraOptions = com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(location.lng, location.lat))
                .zoom(16.5)
                .pitch(0.0)
                .bearing(0.0)
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions {
                duration(1000) // 1秒のアニメーション
            }
        )
    }
}
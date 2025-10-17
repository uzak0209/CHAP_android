package com.back.chap.components

import com.back.chap.models.Coordinate
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState


fun ToggleDimension(
    viewportState: MapViewportState,
    is3D: Boolean,
    coordinate: Coordinate?
) {
    // カメラオプション更新: ピッチとベアリングをトグル
    viewportState.setCameraOptions {
        pitch(if (is3D) 60.0 else 0.0)
        bearing(if (is3D) 45.0 else 0.0)

        // 位置が取れている場合はそこへフォーカス (未取得なら現状維持)
        coordinate?.let { loc ->
            center(Point.fromLngLat(loc.lng,loc.lat))

            if (is3D) {
                // 3D 表示時少しズームアップ
                zoom(16.5)
            }
        }
    }
}

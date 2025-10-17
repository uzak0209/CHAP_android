package com.back.chap.components

import androidx.compose.runtime.Composable
import com.back.chap.models.Mappable
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions

@Composable
fun <T : Mappable> AnnotationMarkers(
    items: List<out T>,
    styleLoaded: Boolean,
    iconImageName: String,
    onItemClick: (T) -> Unit
) {
    if (items.isNotEmpty() && styleLoaded) {
        PointAnnotationGroup(
            annotations = items.map { item ->
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(item.coordinate.lng, item.coordinate.lat))
                    .withIconImage(iconImageName)
                    .withIconSize(1.0)
                // .withIconAnchor(...) も必要であればここに追加
            },
            onClick = { annotation ->
                // クリックされたAnnotationに対応する元のアイテムを探す
                val clickedItem = items.find { item ->
                    annotation.point.latitude() == item.coordinate.lat &&
                            annotation.point.longitude() == item.coordinate.lng
                }

                // 見つかったアイテムをコールバックで通知
                clickedItem?.let {
                    onItemClick(it)
                }
                true
            }
        )
    }
}
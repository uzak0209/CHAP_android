package com.back.chap.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.back.chap.models.Mappable
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

@Composable
fun <T : Mappable> MappableItemPopup(
    item: T?,
    content: @Composable (T) -> Unit
) {
    item?.let {
        ViewAnnotation(
            options = viewAnnotationOptions {
                geometry(Point.fromLngLat(it.coordinate.lng, it.coordinate.lat))
                allowOverlap(true)
                allowOverlapWithPuck(true)
            }
        ) {
            SpeechBubble(lift = 0.dp) {
                content(it)
            }
        }
    }
}
package com.example.chap.screens.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.example.chap.components.AnnotationMarkers
import com.example.chap.components.map.MappableItemPopup
import com.example.chap.components.map.PostPopup
import com.example.chap.components.map.ThreadPopup
import com.example.chap.components.map.EventPopup
import com.example.chap.components.map.SpotPopup
import com.example.chap.models.*
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions

/**
 * マーカー表示の種類を定義
 */
sealed class MarkerType<T : Mappable>(
    val iconImageName: String,
    val logPrefix: String
) {
    object Post : MarkerType<com.example.chap.models.Post>("pin-post", "Post")
    object Thread : MarkerType<com.example.chap.models.Thread>("pin-thread", "Thread")
    object Event : MarkerType<com.example.chap.models.Event>("pin-event", "Event")
    object Spot : MarkerType<com.example.chap.models.Spot>("pin-spot", "Spot")
}

/**
 * すべてのマーカーをまとめて表示
 */
@Composable
fun AllMapMarkers(
    posts: List<Post>,
    threads: List<Thread>,
    events: List<Event>,
    spots: List<Spot>,
    styleLoaded: Boolean,
    onPostClick: (Post) -> Unit,
    onThreadClick: (Thread) -> Unit,
    onEventClick: (Event) -> Unit,
    onSpotClick: (Spot) -> Unit
) {
    // Posts
    MapMarkers(
        items = posts,
        styleLoaded = styleLoaded,
        markerType = MarkerType.Post,
        onItemClick = onPostClick
    )
    
    // Threads
    MapMarkers(
        items = threads,
        styleLoaded = styleLoaded,
        markerType = MarkerType.Thread,
        onItemClick = onThreadClick
    )
    
    // Events
    MapMarkers(
        items = events,
        styleLoaded = styleLoaded,
        markerType = MarkerType.Event,
        onItemClick = onEventClick
    )
    
    // Spots
    MapMarkers(
        items = spots,
        styleLoaded = styleLoaded,
        markerType = MarkerType.Spot,
        onItemClick = onSpotClick
    )
}

/*
 * 汎用マーカー表示（型安全）
 */
@Composable
private fun <T : Mappable> MapMarkers(
    items: List<T>,
    styleLoaded: Boolean,
    markerType: MarkerType<T>,
    onItemClick: (T) -> Unit
) {
    AnnotationMarkers(
        items = items,
        styleLoaded = styleLoaded,
        iconImageName = markerType.iconImageName,
        onItemClick = { item ->
            onItemClick(item)
            println("[MapScreen] ${markerType.logPrefix} marker clicked: $item")
        }
    )
}

/**
 * 一時マーカー（イベント位置選択中）
 */
@Composable
fun TempEventMarker(
    coordinate: Coordinate?,
    styleLoaded: Boolean
) {
    if (coordinate != null && styleLoaded) {
        PointAnnotationGroup(
            annotations = listOf(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(coordinate.lng, coordinate.lat))
                    .withIconImage("pin-event")
                    .withIconSize(1.0)
            ),
            onClick = { false }
        )
    }
}

/**
 * すべてのポップアップをまとめて表示
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllMapPopups(
    selectedPost: Post?,
    selectedThread: Thread?,
    selectedEvent: Event?,
    selectedSpot: Spot?,
    onPostDismiss: () -> Unit,
    onThreadDismiss: () -> Unit,
    onEventDismiss: () -> Unit,
    onSpotDismiss: () -> Unit
) {
    selectedPost?.let { post ->
        MappableItemPopup(post) {
            PostPopup(post = post, onDismiss = onPostDismiss)
        }
    }

    selectedThread?.let { thread ->
        MappableItemPopup(thread) {
            ThreadPopup(thread = thread, onDismiss = onThreadDismiss)
        }
    }

    selectedEvent?.let { event ->
        MappableItemPopup(event) {
            EventPopup(event = event, onDismiss = onEventDismiss)
        }
    }

    selectedSpot?.let { spot ->
        MappableItemPopup(spot) {
            SpotPopup(spot = spot, onDismiss = onSpotDismiss)
        }
    }
}


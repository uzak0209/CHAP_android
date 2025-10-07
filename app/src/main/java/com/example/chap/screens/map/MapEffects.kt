package com.example.chap.screens.map

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import com.example.chap.R
import com.example.chap.components.map.bitmapFromVector
import com.example.chap.models.Coordinate
import com.example.chap.models.CreateKind
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar

/**
 * すべてのMapEffectをまとめて管理
 */
@Composable
fun MapEffects(
    state: MapState,
    drawerState: DrawerState,
) {
    // スタイルロード & ピン登録
    StyleLoadEffect(
        styleLoaded = state.styleLoaded,
        onStyleLoaded = { state.styleLoaded = true }
    )
    
    // 位置情報プラグイン有効化
    LocationPluginEffect()
    
    // ジェスチャー制御
    GestureControlEffect(
        showPopup = state.showPopup,
        drawerState = drawerState,
        hasSelection = state.hasSelection()
    )
    
    // イベント位置選択リスナー
    EventLocationPickerEffect(
        createKind = state.createKind,
        pendingEventDraft = state.pendingEventDraft,
        mapTapPickListener = state.mapTapPickListener,
        onMapTapPickListenerChange = { state.mapTapPickListener = it },
        onCoordinatePicked = { state.pendingTapCoordinate = it }
    )
    
    // スケールバー無効化
    ScalebarDisableEffect(styleLoaded = state.styleLoaded)
    
    // タップで選択解除
    SelectionClearEffect(
        hasSelection = state.hasSelection(),
        mapTapCloseListener = state.mapTapCloseListener,
        onMapTapCloseListenerChange = { state.mapTapCloseListener = it },
        onClearSelection = { state.clearSelection() }
    )
}

@Composable
private fun StyleLoadEffect(
    styleLoaded: Boolean,
    onStyleLoaded: () -> Unit
) {
    MapEffect(Unit) { mapView ->
        val mbMap = mapView.mapboxMap
        if (!styleLoaded) {
            mbMap.loadStyleUri(Style.STANDARD) {
                try {
                    val ctx = mapView.context
                    mbMap.style?.apply {
                        addImage("pin-post", bitmapFromVector(ctx, R.drawable.marker_blue))
                        addImage("pin-event", bitmapFromVector(ctx, R.drawable.marker_red))
                        addImage("pin-thread", bitmapFromVector(ctx, R.drawable.marker_yellow))
                        addImage("pin-spot", bitmapFromVector(ctx, R.drawable.pin_red))
                    }
                    println("[MapScreen] Registered SVG pins")
                } catch (e: Exception) {
                    println("[MapScreen] Failed to register pins: ${e.message}")
                }
                onStyleLoaded()
            }
        }
    }
}

@Composable
private fun LocationPluginEffect() {
    MapEffect(Unit) { mapView ->
        mapView.location.updateSettings { 
            enabled = true
            pulsingEnabled = true
        }
    }
}

@Composable
private fun GestureControlEffect(
    showPopup: Boolean,
    drawerState: DrawerState,
    hasSelection: Boolean
) {
    MapEffect(Triple(showPopup, drawerState.currentValue, hasSelection)) { mapView ->
        runCatching { mapView.gestures }
            .getOrNull()
            ?.updateSettings {
                val allowMapGestures = !showPopup && 
                    drawerState.currentValue != DrawerValue.Open
                scrollEnabled = allowMapGestures
                pinchToZoomEnabled = allowMapGestures
                rotateEnabled = allowMapGestures
                quickZoomEnabled = allowMapGestures
                pitchEnabled = allowMapGestures
            }
    }
}

@Composable
private fun EventLocationPickerEffect(
    createKind: CreateKind,
    pendingEventDraft: Triple<String, com.example.chap.models.PostCategory, List<String>>?,
    mapTapPickListener: OnMapClickListener?,
    onMapTapPickListenerChange: (OnMapClickListener?) -> Unit,
    onCoordinatePicked: (Coordinate) -> Unit
) {
    MapEffect(createKind, pendingEventDraft) { mapView ->
        mapTapPickListener?.let { mapView.gestures.removeOnMapClickListener(it) }
        if (createKind == CreateKind.EVENT && pendingEventDraft != null) {
            val listener = OnMapClickListener { point ->
                onCoordinatePicked(Coordinate(lat = point.latitude(), lng = point.longitude()))
                true
            }
            mapView.gestures.addOnMapClickListener(listener)
            onMapTapPickListenerChange(listener)
        }
    }
}

@Composable
private fun ScalebarDisableEffect(styleLoaded: Boolean) {
    MapEffect(styleLoaded) { mapView ->
        if (styleLoaded) {
            runCatching { mapView.scalebar.updateSettings { enabled = false } }
        }
    }
}

@Composable
private fun SelectionClearEffect(
    hasSelection: Boolean,
    mapTapCloseListener: OnMapClickListener?,
    onMapTapCloseListenerChange: (OnMapClickListener?) -> Unit,
    onClearSelection: () -> Unit
) {
    MapEffect(hasSelection) { mapView ->
        mapTapCloseListener?.let { mapView.gestures.removeOnMapClickListener(it) }
        
        if (hasSelection) {
            val listener = OnMapClickListener {
                onClearSelection()
                true
            }
            mapView.gestures.addOnMapClickListener(listener)
            onMapTapCloseListenerChange(listener)
        } else {
            onMapTapCloseListenerChange(null)
        }
    }
}


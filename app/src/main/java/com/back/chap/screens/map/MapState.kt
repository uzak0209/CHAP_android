package com.back.chap.screens.map

import androidx.compose.runtime.*
import com.back.chap.models.*
import com.mapbox.maps.plugin.gestures.OnMapClickListener

/**
 * マップ画面の状態管理
 */
@Stable
class MapState {
    var is3D by mutableStateOf(true)
    var styleLoaded by mutableStateOf(false)
    var showPopup by mutableStateOf(false)
    var showCreate by mutableStateOf(false)
    var createKind by mutableStateOf(CreateKind.POST)
    var pendingTapCoordinate by mutableStateOf<Coordinate?>(null)
    var pendingEventDraft by mutableStateOf<Triple<String, String, String>?>(null) // (content/title, category/description, imageUrl)
    var selectedPost by mutableStateOf<Post?>(null)
    var selectedThread by mutableStateOf<Thread?>(null)
    var selectedEvent by mutableStateOf<Event?>(null)
    var selectedSpot by mutableStateOf<Spot?>(null)
    var mapTapCloseListener by mutableStateOf<OnMapClickListener?>(null)
    var mapTapPickListener by mutableStateOf<OnMapClickListener?>(null)
    
    fun clearSelection() {
        selectedPost = null
        selectedThread = null
        selectedEvent = null
        selectedSpot = null
    }
    
    fun hasSelection(): Boolean {
        return selectedPost != null || selectedThread != null || 
               selectedEvent != null || selectedSpot != null
    }
}

@Composable
fun rememberMapState() = remember { MapState() }


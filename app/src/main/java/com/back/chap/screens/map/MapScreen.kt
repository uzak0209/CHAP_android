package com.back.chap.screens.map

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.back.chap.components.ToggleDimension
import com.back.chap.components.map.SlidBar
import com.back.chap.components.map.moveViewPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.launch

/*
 * メイン地図画面
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
    onNavigateSetting: () -> Unit,
    onNavigateTimeline: () -> Unit = {},
    onNavigateRecord: () -> Unit = {},
    locationViewModel: LocationViewModel,
    focusType: String? = null,
    focusId: String? = null
) {
    // 状態管理
    val state = rememberMapState()
    
    // ViewModelからデータを監視
    val posts by locationViewModel.posts.collectAsState()
    val threads by locationViewModel.threads.collectAsState()
    val events by locationViewModel.events.collectAsState()
    val spots by locationViewModel.spots.collectAsState()
    val locationState by locationViewModel.locationState.collectAsState()

    // 位置情報パーミッション要求と初期ロード
    var permissionRequested by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationViewModel.fetchAndUpdateLocation()
            locationViewModel.load()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // カメラ状態
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(16.5)
            center(Point.fromLngLat(
                locationState.location?.lng ?: 0.0,
                locationState.location?.lat ?: 0.0
            ))
            pitch(0.0)
            bearing(0.0)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            SlidBar(
                onNavigateHome = onNavigateHome,
                onNavigateEvent = onNavigateEvent,
                onNavigateThread = onNavigateThread,
                onNavigateSetting = onNavigateSetting,
                onNavigateRecord = onNavigateRecord,
                onNavigateTimeline = onNavigateTimeline,
                spots = spots,
                onSpotClick = { spot ->
                    moveViewPoint(viewportState, scope, spot.coordinate)
                }
            )
        }
    ) {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEEEEEE))
                ) {
                    // 地図本体（最背面）
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = viewportState,
                        style = { Style.STANDARD }
                    ) {
                        // MapEffects
                        MapEffects(
                            state = state,
                            drawerState = drawerState
                        )

                        // マーカー表示
                        AllMapMarkers(
                            posts = posts,
                            threads = threads,
                            events = events,
                            spots = spots,
                            styleLoaded = state.styleLoaded,
                            onPostClick = { state.selectedPost = it },
                            onThreadClick = { state.selectedThread = it },
                            onEventClick = { state.selectedEvent = it },
                            onSpotClick = { state.selectedSpot = it }
                        )

                        // 一時マーカー（イベント位置選択中）
                        if (state.pendingEventDraft != null) {
                            TempEventMarker(
                                coordinate = state.pendingTapCoordinate,
                                styleLoaded = state.styleLoaded
                            )
                        }

                        // ポップアップ表示
                        AllMapPopups(
                            selectedPost = state.selectedPost,
                            selectedThread = state.selectedThread,
                            selectedEvent = state.selectedEvent,
                            selectedSpot = state.selectedSpot,
                            onPostDismiss = { state.selectedPost = null },
                            onThreadDismiss = { state.selectedThread = null },
                            onEventDismiss = { state.selectedEvent = null },
                            onSpotDismiss = { state.selectedSpot = null }
                        )
                    }

                    // タイムラインからのフォーカス要求を一度だけ処理
                    var focusHandled by remember { mutableStateOf(false) }
                    LaunchedEffect(focusType, focusId, posts, threads, events) {
                        if (!focusHandled && focusType != null && focusId != null) {
                            when (focusType) {
                                "post" -> {
                                    posts.firstOrNull { it.id.toString() == focusId }?.let { p ->
                                        state.selectedPost = p
                                        moveViewPoint(viewportState, scope, p.coordinate)
                                        focusHandled = true
                                    }
                                }
                                "thread" -> {
                                    threads.firstOrNull { it.id == focusId }?.let { t ->
                                        state.selectedThread = t
                                        moveViewPoint(viewportState, scope, t.coordinate)
                                        focusHandled = true
                                    }
                                }
                                "event" -> {
                                    events.firstOrNull { it.id.toString() == focusId }?.let { e ->
                                        state.selectedEvent = e
                                        moveViewPoint(viewportState, scope, e.coordinate)
                                        focusHandled = true
                                    }
                                }
                                "spot" -> {
                                    spots.firstOrNull { it.id == focusId }?.let { e ->
                                        state.selectedSpot = e
                                        moveViewPoint(viewportState, scope, e.coordinate)
                                        focusHandled = true
                                    }
                                }
                            }
                        }
                    }
                    // ヘッダー（最前面）
                    if (state.pendingEventDraft == null) {
                        MapHeader(
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onRecordClick = onNavigateRecord
                        )
                    }

                    // オーバーレイUI（地図の上に重ねる）
                    MapOverlays(
                        state = state,
                        locationState = locationState,
                        scope = scope,
                        locationViewModel = locationViewModel,
                        is3D = state.is3D,
                        onToggleDimension = {
                            state.is3D = !state.is3D
                            ToggleDimension(viewportState, state.is3D, locationState.location)
                        },
                        onLocationClick = {
                            moveViewPoint(viewportState, scope, locationState.location)
                        },
                    )
                }
            }
        }
    }
}

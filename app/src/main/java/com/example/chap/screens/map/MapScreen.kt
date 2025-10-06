package com.example.chap.screens.map

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.models.CreateKind
import dagger.hilt.android.EntryPointAccessors
import com.example.chap.components.CreateDialog
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.ToggleDimension
import com.example.chap.components.map.SlidBar
import com.example.chap.components.map.PostPopup
import com.example.chap.components.map.ThreadPopup
import com.example.chap.components.map.EventPopup
import com.example.chap.components.map.SpeechBubble
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotationGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import android.graphics.drawable.Drawable
import com.example.chap.R
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Menu
import com.example.chap.components.AnnotationMarkers
import com.example.chap.components.map.MappableItemPopup
import com.example.chap.components.map.SpotPopup
import com.example.chap.components.map.bitmapFromVector
import com.example.chap.components.map.moveViewPoint
import com.example.chap.models.Coordinate
import com.example.chap.models.Event
import com.example.chap.models.Spot
import com.example.chap.screens.event.EventViewModel
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.ui.theme.BrandBlue
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlinx.coroutines.launch
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.example.chap.models.Mappable


/**
 * Mappableアイテムのポップアップを表示するヘルパーComposable
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
    onNavigateSetting: ()-> Unit,
    onNavigateTimeline: () -> Unit = {},
    onNavigateRecord: () -> Unit = {},
    locationViewModel: LocationViewModel
) {
    // Compose で ViewModel の位置情報を監視
    var is3D by remember { mutableStateOf(true) }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf(CreateKind.POST) }
    var pendingTapCoordinate by remember { mutableStateOf<Coordinate?>(null) }
    var pendingEventDraft by remember { mutableStateOf<Triple<String, com.example.chap.models.PostCategory, List<String>>?>(null) }

    // ViewModelからデータを監視
    val posts by locationViewModel.posts.collectAsState()
    val threads by locationViewModel.threads.collectAsState()
    val events by locationViewModel.events.collectAsState()
    val spots by locationViewModel.spots.collectAsState()
    val locationState by locationViewModel.locationState.collectAsState()

    // デバッグログ
    LaunchedEffect(posts.size) {
        println("[MapScreen] Posts count: ${posts.size}")
        posts.forEach { post ->
            println("[MapScreen] Post ID: ${post.id}, Content: ${post.content}, Lat: ${post.coordinate.lat}, Lng: ${post.coordinate.lng}")
        }
    }

    // 選択されたアイテムとポップアップ表示状態
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var selectedThread by remember { mutableStateOf<Thread?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var selectedSpot by remember { mutableStateOf<Spot?>(null) }
    var mapTapCloseListener by remember { mutableStateOf<OnMapClickListener?>(null) }
    var mapTapPickListener by remember { mutableStateOf<OnMapClickListener?>(null) }
 

    // 位置情報を取得（ViewModel 経由）
    LaunchedEffect(Unit) {
        locationViewModel.fetchAndUpdateLocation()
        locationViewModel.load()
    }

    // Mapbox カメラ状態
    val viewportState = rememberMapViewportState {
        println("Camera position: ${locationViewModel.locationState.value.location}")
        setCameraOptions {
            zoom(16.5)
            center(
                Point.fromLngLat(
                    locationState.location?.lng ?: 0.0,
                    locationState.location?.lat ?: 0.0
                )
            )
            pitch(0.0)
            bearing(0.0)
        }
    }

    //サイドバーの開閉を監視
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        //マップ操作をサイドバーが邪魔しない
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            SlidBar(
                onNavigateHome = onNavigateHome,
                onNavigateEvent = onNavigateEvent,
                onNavigateThread = onNavigateThread,
                spots = spots,
                onSpotClick = { spot ->
                    moveViewPoint(
                        viewportState,
                        scope,
                        spot.coordinate,
                    )
                }
            )
        }
    ) {
        Scaffold{ innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = viewportState,
                        style = {
                            // Mapbox Standard スタイルは自動的に端末の言語でラベルを表示
                            Style.STANDARD
                        }
                    ) {
                        
                        MapEffect(Unit) { mapView ->
                            val mbMap = mapView.mapboxMap
                            if (!styleLoaded) {
                                mbMap.loadStyleUri(Style.STANDARD) {
                                    // スタイルロード後に提供SVGを登録
                                    try {
                                        val ctx = mapView.context
                                        //ピンを登録
                                        mbMap.style?.addImage("pin-post", bitmapFromVector(ctx, R.drawable.marker_blue))
                                        mbMap.style?.addImage("pin-event", bitmapFromVector(ctx, R.drawable.marker_red))
                                        mbMap.style?.addImage("pin-thread", bitmapFromVector(ctx, R.drawable.marker_yellow))
                                        mbMap.style?.addImage("pin-spot", bitmapFromVector(ctx, R.drawable.pin_red))
                                        println("[MapScreen] Registered SVG pins: post/event/thread")
                                    } catch (e: Exception) {
                                        println("[MapScreen] Failed to register SVG pins: ${e.message}")
                                    }
                                    styleLoaded = true
                                }
                            }
                        }
                        // 位置情報プラグインを有効化（1回で十分）
                        MapEffect(Unit) { mapView ->
                            val plugin = mapView.location
                            plugin.updateSettings { enabled = true; pulsingEnabled = true }
                        }
                        // Disable map gestures while popup overlay is visible OR drawer is open OR item is selected
                        MapEffect(
                            Triple(showPopup, drawerState.currentValue, selectedPost != null || selectedThread != null || selectedEvent != null)
                        ) { mapView ->
                            //ユーザーのタッチ操作を管理するGesturesPluginインスタンスを取得しようとしている
                            runCatching { mapView.gestures }
                                .getOrNull()
                                ?.updateSettings {
                                    //ビューアノテーションでのポップアップ表示中も地図操作は可能にする
                                    val allowMapGestures = !showPopup &&
                                                          drawerState.currentValue != DrawerValue.Open
                                    scrollEnabled = allowMapGestures
                                    pinchToZoomEnabled = allowMapGestures
                                    rotateEnabled = allowMapGestures
                                    quickZoomEnabled = allowMapGestures
                                    pitchEnabled = allowMapGestures
                                }
                        }

                        // イベント作成時は「ダイアログ送信後にタップで位置を選択」モードにする
                        MapEffect(createKind, showCreate, pendingEventDraft) { mapView ->
                            // 既存のピック用タップリスナーのみ解除
                            mapTapPickListener?.let { mapView.gestures.removeOnMapClickListener(it) }
                            if (createKind == CreateKind.EVENT && pendingEventDraft != null) {
                                val listener = object : OnMapClickListener {
                                    override fun onMapClick(point: Point): Boolean {
                                        val tapped = Coordinate(lat = point.latitude(), lng = point.longitude())
                                        pendingTapCoordinate = tapped
                                        return true
                                    }
                                }
                                mapView.gestures.addOnMapClickListener(listener)
                                mapTapPickListener = listener
                            }
                        }

                        MapEffect(styleLoaded) { mapView ->
                            if (styleLoaded) {
                                try {
                                    mapView.scalebar.updateSettings { enabled = false }
                                } catch (_: Exception) {}
                            }
                        }

                        // マップ外側タップでポップアップを閉じる（選択状態に応じて追加/削除）
                        MapEffect(selectedPost, selectedThread, selectedEvent, selectedSpot) { mapView ->
                            // 既存リスナーを一旦解除
                            mapTapCloseListener?.let { mapView.gestures.removeOnMapClickListener(it) }

                            val hasSelection = selectedPost != null || selectedThread != null || selectedEvent != null || selectedSpot != null
                            if (hasSelection) {
                                val listener = OnMapClickListener {
                                    val stillSelected = selectedPost != null || selectedThread != null || selectedEvent != null || selectedSpot != null
                                    if (stillSelected) {
                                        selectedPost = null
                                        selectedThread = null
                                        selectedEvent = null
                                        selectedSpot = null
                                        true
                                    } else {
                                        false
                                    }
                                }
                                mapView.gestures.addOnMapClickListener(listener)
                                mapTapCloseListener = listener
                            } else {
                                mapTapCloseListener = null
                            }
                        }

                        // ポストマーカーを表示
                        AnnotationMarkers(
                            items = posts,
                            styleLoaded = styleLoaded,
                            iconImageName = "pin-post",
                            onItemClick = { clickedPost : Post->
                                selectedPost = clickedPost
                                println("[MapScreen] Post marker clicked: $clickedPost")
                            }
                        )
                        
                        // スレッドマーカーを表示
                        AnnotationMarkers(
                            items = threads,
                            styleLoaded = styleLoaded,
                            iconImageName = "pin-thread",
                            onItemClick = { clickedThread : Thread->
                                selectedThread = clickedThread
                                println("[MapScreen] Thread marker clicked: $clickedThread")
                            }
                        )

                        AnnotationMarkers(
                            items = events,
                            styleLoaded = styleLoaded,
                            iconImageName = "pin-event",
                            onItemClick = { clickedEvent : Event->
                                selectedEvent = clickedEvent
                                println("[MapScreen] Event marker clicked: $clickedEvent")
                            }
                        )
                        AnnotationMarkers(
                            items = spots,
                            styleLoaded = styleLoaded,
                            iconImageName = "pin-spot",
                            onItemClick = { clickedSpot : Spot->
                                selectedSpot = clickedSpot
                                println("[MapScreen] Spot marker clicked: $clickedSpot")
                            }
                        )

                        // 一時マーカー（イベント位置選択中）
                        if (pendingEventDraft != null && pendingTapCoordinate != null && styleLoaded) {
                            PointAnnotationGroup(
                                annotations = listOf(
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(pendingTapCoordinate!!.lng, pendingTapCoordinate!!.lat))
                                        .withIconImage("pin-event")
                                        .withIconSize(1.0)
                                ),
                                onClick = { false }
                            )
                        }

                        // --- ViewAnnotation based popups anchored to selected items ---
                        MappableItemPopup(selectedPost) { post ->
                            PostPopup(
                                post = post,
                                onDismiss = { selectedPost = null }
                            )
                        }

                        MappableItemPopup(selectedThread) { thread ->
                            ThreadPopup(
                                thread = thread,
                                onDismiss = { selectedThread = null }
                            )
                        }

                        MappableItemPopup(selectedEvent) { event ->
                            EventPopup(
                                event = event,
                                onDismiss = { selectedEvent = null }
                            )
                        }

                        MappableItemPopup(selectedSpot) { spot ->
                            SpotPopup(
                                spot = spot,
                                onDismiss = { selectedSpot = null }
                            )
                        }
                    }
                    if (pendingEventDraft == null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) { Icon(Icons.Default.Menu, contentDescription = "Open navigation", tint = Color.White) }
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    is3D = !is3D
                                    ToggleDimension(
                                        viewportState,
                                        is3D,
                                        coordinate = locationState.location
                                        )
                                }
                            ) { Text(text = if (is3D) "2D" else "3D", color = Color.White) }
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    moveViewPoint(
                                        viewportState,
                                        scope,
                                        locationViewModel.locationState.value.location,
                                    )
                                }
                            ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    onNavigateSetting()
                                }
                            ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    onNavigateTimeline()
                                }
                            ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
                            FloatingActionButton(
                                modifier = Modifier,
                                containerColor = BrandBlue,
                                onClick = {
                                    onNavigateRecord()
                                }
                            ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
                        }
                    }
                    // ガイド（上部）: 位置選択中の案内
                    if (pendingEventDraft != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = {},
                                containerColor = BrandBlue
                            ) {
                                Text(text = "タップして位置を決めてください", color = Color.White)
                            }
                        }
                    }
                    //投稿作成ボタン（位置選択中は非表示）
                    if (pendingEventDraft == null) {
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            onClick = { showPopup = true },
                            containerColor = BrandBlue,
                        ) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    }

                    // 決定ボタン（下部）: 位置選択中のみ表示
                    if (pendingEventDraft != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    val draft = pendingEventDraft
                                    val coord = pendingTapCoordinate
                                    if (draft != null && coord != null) {
                                        val (contentDraft, categoryDraft, tagsDraft) = draft
                                        val createObject = com.example.chap.models.PostCreateRequest(
                                            coordinate = coord,
                                            content = contentDraft,
                                            category = categoryDraft.toString(),
                                            valid = true,
                                            tags = tagsDraft,
                                            visible = true
                                        )
                                        scope.launch {
                                            try {
                                                locationViewModel.createEvent(createObject)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                pendingEventDraft = null
                                                pendingTapCoordinate = null
                                            }
                                        }
                                    }
                                },
                                containerColor = if (pendingTapCoordinate != null) BrandBlue else Color.Gray
                            ) {
                                Text(text = "この位置で作成", color = Color.White)
                            }
                        }
                    }

                    CreateDialog(
                        isOpen = showCreate,
                        onClose = { showCreate = false },
                        selectedKind = createKind,
                        locationViewModel = locationViewModel,
                        coordinate = if (createKind == CreateKind.EVENT) pendingTapCoordinate else locationState.location,
                        onRequestEventLocation = { contentDraft, categoryDraft, tagsDraft ->
                            // ダイアログの「投稿」押下後に、次のタップで場所決定
                            pendingEventDraft = Triple(contentDraft, categoryDraft, tagsDraft)
                        }
                    )

                    SelectPopupOverlay(
                        visible = showPopup,
                        onDismiss = { showPopup = false },
                        onPostCreated = {
                            showPopup = false; createKind = CreateKind.POST; showCreate = true
                        },
                        onThreadCreated = {
                            showPopup = false; createKind = CreateKind.THREAD; showCreate = true
                        },
                        onEventCreated = {
                            showPopup = false; createKind = CreateKind.EVENT; showCreate = true
                        },
                        registerLocation = {
                            showPopup = false; createKind = CreateKind.SPOT; showCreate = true
                        }
                    )
                    
                    // フルスクリーンの半透明オーバーレイは廃止（ViewAnnotationで表示）
                    
                    if (!styleLoaded) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("地図スタイル読み込み中…", color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}



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
import com.example.chap.components.map.SpotPopup
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
    locationViewModel: LocationViewModel
) {
    // Compose で ViewModel の位置情報を監視
    var is3D by remember { mutableStateOf(true) }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf(CreateKind.POST) }

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
                        locationViewModel
                    )
                }
            )
        }
    ) {
        Scaffold{ innerPadding ->
            // innerPadding を適用して画面本体を表示
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = viewportState,
                        style = { Style.STANDARD }
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
                                    //ジェスチャー許可を管理
                                    val allowMapGestures = !showPopup &&
                                                          drawerState.currentValue != DrawerValue.Open &&
                                                          selectedPost == null &&
                                                          selectedThread == null &&
                                                          selectedEvent == null
                                    scrollEnabled = allowMapGestures
                                    pinchToZoomEnabled = allowMapGestures
                                    rotateEnabled = allowMapGestures
                                    quickZoomEnabled = allowMapGestures
                                    pitchEnabled = allowMapGestures
                                }
                        }

                        MapEffect(styleLoaded) { mapView ->
                            if (styleLoaded) {
                                runCatching { mapView.scalebar }
                                    .getOrNull()
                                    ?.updateSettings { enabled = false }
                            }
                        }

                        // ポストマーカーを表示
                        if (posts.isNotEmpty() && styleLoaded) {
                            println("[MapScreen] Created post")
                            PointAnnotationGroup(
                                annotations = posts.map { post ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(post.coordinate.lng, post.coordinate.lat))
                                        .withIconImage("pin-post")
                                        .withIconSize(1.0)
                                },
                                onClick = { annotation ->
                                    // マーカークリック時の処理
                                    val clickedPost = posts.find { post ->
                                        annotation.point.latitude() == post.coordinate.lat &&
                                        annotation.point.longitude() == post.coordinate.lng
                                    }
                                    selectedPost = clickedPost
                                    println("[MapScreen] Clicked post: ${clickedPost?.content}")
                                    true
                                }
                            )
                        }else{
                            println("まだポストはロードされてません")
                        }
                        
                        // スレッドマーカーを表示
                        if (threads.isNotEmpty() && styleLoaded) {
                            println("[MapScreen] Created thread")
                            PointAnnotationGroup(
                                annotations = threads.map { thread ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(thread.coordinate.lng, thread.coordinate.lat))
                                        .withIconImage("pin-thread")
                                        .withIconSize(1.0)
//                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    val clickedThread = threads.find { thread ->
                                        annotation.point.latitude() == thread.coordinate.lat &&
                                        annotation.point.longitude() == thread.coordinate.lng
                                    }
                                    selectedThread = clickedThread
                                    true
                                }
                            )
                        }else{
                            println("まだスレッドはロードされてません")
                        }
                        
                        // イベントマーカーを表示
                        if (events.isNotEmpty() && styleLoaded) {
                            println("[MapScreen] Created event")
                            PointAnnotationGroup(
                                annotations = events.map { event ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(event.coordinate.lng, event.coordinate.lat))
                                        .withIconImage("pin-event")
                                        .withIconSize(1.0)
//                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    val clickedEvent = events.find { event ->
                                        annotation.point.latitude() == event.coordinate.lat &&
                                        annotation.point.longitude() == event.coordinate.lng
                                    }
                                    selectedEvent = clickedEvent
                                    true
                                }
                            )
                        }else{
                            println("まだイベントはロードされてません")
                        }
                        if (spots.isNotEmpty() && styleLoaded) {
                            println("[MapScreen] Created spot")
                            PointAnnotationGroup(
                                annotations = spots.map { spot ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(spot.coordinate.lng, spot.coordinate.lat))
                                        .withIconImage("pin-spot")
                                        .withIconSize(1.0)
//                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    val clickedSpot = spots.find { spot ->
                                        annotation.point.latitude() == spot.coordinate.lat &&
                                                annotation.point.longitude() == spot.coordinate.lng
                                    }
                                    selectedSpot = clickedSpot
                                    true
                                }
                            )
                        }else{
                            println("まだスポットはロードされてません")
                        }
                    }
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
                                    locationViewModel,
                                )
                            }
                        ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
                    }
                    //投稿作成ボタン
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        onClick = { showPopup = true },
                        containerColor = BrandBlue,
                    ) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                    CreateDialog(
                        isOpen = showCreate,
                        onClose = { showCreate = false },
                        selectedKind = createKind,
                        locationViewModel = locationViewModel,
                        coordinate = locationState.location
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
                    
                    // 選択された投稿のポップアップ
                    selectedPost?.let { post ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedPost = null },
                            contentAlignment = Alignment.Center
                        ) {
                            PostPopup(
                                post = post,
                                onDismiss = { selectedPost = null }
                            )
                        }
                    }
                    
                    // 選択されたスレッドのポップアップ
                    selectedThread?.let { thread ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedThread = null },
                            contentAlignment = Alignment.Center
                        ) {
                            ThreadPopup(
                                thread = thread,
                                onDismiss = { selectedThread = null }
                            )
                        }
                    }
                    
                    // 選択されたイベントのポップアップ
                    selectedEvent?.let { event ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedEvent = null },
                            contentAlignment = Alignment.Center
                        ) {
                            EventPopup(
                                event = event,
                                onDismiss = { selectedEvent = null }
                            )
                        }
                    }
                    // 選択されたスポットのポップアップ
                    selectedSpot?.let { spot ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedSpot = null },
                            contentAlignment = Alignment.Center
                        ) {
                            SpotPopup(
                                spot= spot,
                                onDismiss = { selectedSpot = null }
                            )
                        }
                    }
                    
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

/**
 * 地図ピンアイコンを生成する関数（画像の形状に準拠）
 * @param color ピンの色
 * @return ビットマップ画像
 */
private fun bitmapFromVector(context: Context, drawableResId: Int): Bitmap {
    val drawable: Drawable = requireNotNull(AppCompatResources.getDrawable(context, drawableResId))
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

/**
 * 地図の視点を現在地に戻す関数
 */

fun moveViewPoint(
    viewportState: MapViewportState,
    scope: kotlinx.coroutines.CoroutineScope,
    location: Coordinate?,
    locationViewModel: LocationViewModel
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
